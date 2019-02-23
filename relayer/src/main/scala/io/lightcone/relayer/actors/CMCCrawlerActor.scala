/*
 * Copyright 2018 Loopring Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lightcone.relayer.actors

import akka.actor._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import io.lightcone.core._
import io.lightcone.lib._
import io.lightcone.persistence._
import io.lightcone.relayer.base._
import io.lightcone.relayer.data.cmc._
import io.lightcone.relayer.external._
import scala.concurrent.{ExecutionContext, Future}
import io.lightcone.relayer.jsonrpc._
import scala.util.{Failure, Success}

// Owner: Yongfeng
object CMCCrawlerActor extends DeployedAsSingleton {
  val name = "cmc_crawler"

  def start(
      implicit
      system: ActorSystem,
      config: Config,
      ec: ExecutionContext,
      timeProvider: TimeProvider,
      timeout: Timeout,
      dbModule: DatabaseModule,
      actors: Lookup[ActorRef],
      materializer: ActorMaterializer,
      externalTickerFetcher: ExternalTickerFetcher,
      fiatExchangeRateFetcher: FiatExchangeRateFetcher,
      metadataManager: MetadataManager,
      deployActorsIgnoringRoles: Boolean
    ): ActorRef = {
    startSingleton(Props(new CMCCrawlerActor()))
  }
}

class CMCCrawlerActor(
  )(
    implicit
    val config: Config,
    val ec: ExecutionContext,
    val timeProvider: TimeProvider,
    val timeout: Timeout,
    val actors: Lookup[ActorRef],
    val materializer: ActorMaterializer,
    val dbModule: DatabaseModule,
    val externalTickerFetcher: ExternalTickerFetcher,
    val fiatExchangeRateFetcher: FiatExchangeRateFetcher,
    val metadataManager: MetadataManager,
    val system: ActorSystem)
    extends InitializationRetryActor
    with JsonSupport
    with RepeatedJobActor
    with ActorLogging {

  val selfConfig = config.getConfig(CMCCrawlerActor.name)
  val refreshIntervalInSeconds = selfConfig.getInt("refresh-interval-seconds")
  val initialDelayInSeconds = selfConfig.getInt("initial-delay-in-seconds")

  private val tokens = metadataManager.getTokens().map(_.meta.symbol)
  private val marketQuoteTokens =
    metadataManager.getMarkets().map(_.quoteTokenSymbol).toSet
  private val effectiveMarketSymbols = metadataManager
    .getMarkets()
    .filter(_.status != MarketMetadata.Status.TERMINATED)
    .map(m => (m.baseTokenSymbol, m.quoteTokenSymbol))

  private var tickers: Seq[ExternalTicker] =
    Seq.empty[ExternalTicker]
  private var slugSymbols
    : Seq[CMCTickerConfig] = Seq.empty[CMCTickerConfig] // slug -> symbol
  private var allTickersInUSD: Seq[ExternalTokenTickerInfo] =
    Seq.empty[ExternalTokenTickerInfo] // USD price
  private var allTickersInCNY: Seq[ExternalTokenTickerInfo] =
    Seq.empty[ExternalTokenTickerInfo] // CNY price
  private var effectiveMarketTickers: Seq[ExternalMarketTickerInfo] =
    Seq.empty[ExternalMarketTickerInfo] // price represent exchange rate of market (price of market LRC-WETH is 0.01)

  val repeatedJobs = Seq(
    Job(
      name = "sync_cmc_datas",
      dalayInSeconds = refreshIntervalInSeconds,
      initialDalayInSeconds = initialDelayInSeconds,
      run = () => syncFromCMC()
    )
  )

  override def initialize() = {
    val f = for {
      latestEffectiveRequest <- dbModule.externalTickerDal
        .getLastTicker()
      (tickers_, slugSymbols_) <- if (latestEffectiveRequest.nonEmpty) {
        for {
          t <- dbModule.externalTickerDal.getTickers(
            latestEffectiveRequest.get
          )
          s <- dbModule.cmcTickerConfigDal.getAll()
        } yield (t, s)
      } else {
        Future.successful((Seq.empty, Seq.empty))
      }
    } yield {
      if (tickers_.nonEmpty && slugSymbols_.nonEmpty) {
        tickers = tickers_
        slugSymbols = slugSymbols_
        refreshTickers()
      }
    }
    f onComplete {
      case Success(_) =>
        becomeReady()
      case Failure(e) =>
        throw e
    }
    f
  }

  def ready: Receive = super.receiveRepeatdJobs orElse {
    case _: GetExternalTokenTickers.Req =>
      sender ! GetExternalTokenTickers.Res(allTickersInUSD)
  }

  private def syncFromCMC() = this.synchronized {
    log.info("CMCCrawlerActor run sync job")
    for {
      cmcResponse <- externalTickerFetcher.fetchExternalTickers()
      rateResponse <- fiatExchangeRateFetcher.fetchExchangeRates()
      slugSymbols_ <- dbModule.cmcTickerConfigDal.getAll()
      (persistTickers, updated) <- if (cmcResponse.data.nonEmpty && rateResponse > 0 && slugSymbols_.nonEmpty) {
        for {
          tickers_ <- persistTickers(rateResponse, cmcResponse.data)
          // TODO(du):更新tokenInfo表
          tokens <- dbModule.tokenMetadataDal.getTokens()
          _ <- updateTokenPrice(cmcResponse.data, tokens)
        } yield (tickers_, true)
      } else {
        Future.successful((Seq.empty, false))
      }
    } yield {
      assert(cmcResponse.data.nonEmpty)
      assert(rateResponse > 0)
      assert(slugSymbols_.nonEmpty)
      slugSymbols = slugSymbols_
      tickers = persistTickers

      if (updated) {
        refreshTickers()
      }
    }
  }

  private def refreshTickers() = this.synchronized {
    val cnyToUsd =
      tickers.find(_.slug == "rmb")
    assert(cnyToUsd.nonEmpty)
    assert(cnyToUsd.get.priceUsd > 0)
    val tickers_ = tickers.filter(_.slug != "rmb")
    allTickersInUSD = tickers_
      .filter(isEffectiveToken)
      .map(CMCExternalTickerFetcher.convertPersistToExternal(_, slugSymbols))
    allTickersInCNY = tickers_.filter(isEffectiveToken).map { t =>
      val t_ =
        CMCExternalTickerFetcher.convertPersistToExternal(t, slugSymbols)
      assert(t.priceUsd > 0)
      t_.copy(
        price = CMCExternalTickerFetcher.toDouble(
          BigDecimal(t.priceUsd) / BigDecimal(
            cnyToUsd.get.priceUsd
          )
        ),
        volume24H = CMCExternalTickerFetcher.toDouble(
          BigDecimal(t.volume24H) / BigDecimal(
            cnyToUsd.get.priceUsd
          )
        )
      )
    }
    effectiveMarketTickers = CMCExternalTickerFetcher
      .fillAllMarketTickers(tickers_, slugSymbols, effectiveMarketSymbols)
  }

  private def isEffectiveToken(ticker: ExternalTicker): Boolean = {
    val slugSymbol = slugSymbols
      .find(_.slug == ticker.slug)
      .getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found slug: ${ticker.slug} to symbol config"
        )
      )
    tokens.contains(slugSymbol.symbol)
  }

  private def updateTokenPrice(
      usdTickers: Seq[CMCTickerData],
      tokens: Seq[TokenMetadata]
    ) = {
    var changedTokens = Seq.empty[TokenMetadata]
    tokens.foreach { token =>
      val slugSymbolOpt = slugSymbols.find(t => t.symbol == token.symbol)
      if (slugSymbolOpt.isEmpty) {
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found slug for symbol: ${token.symbol}"
        )
      }

      val priceQuote =
        usdTickers
          .find(_.slug == slugSymbolOpt.get.slug)
          .flatMap(_.quote.get("USD"))
      val usdPriceQuote = priceQuote.getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"can not found slug:[${slugSymbolOpt.get.slug}] price in USD"
        )
      )
      val externalData = token.externalData
      if (externalData.get.usdPrice != usdPriceQuote.price) {
        changedTokens = changedTokens :+ token.copy(
          externalData =
            Some(externalData.get.copy(usdPrice = usdPriceQuote.price))
        )
      }
    }
    Future.sequence(changedTokens.map { token =>
      dbModule.tokenMetadataDal
        .updateTokenPrice(token.address, token.externalData.get.usdPrice)
        .map { r =>
          if (r != ErrorCode.ERR_NONE)
            log.error(s"failed to update token price:$token")
        }
    })
  }

  private def persistTickers(
      cnyToUsdRate: Double,
      tickers_ : Seq[CMCTickerData]
    ) =
    for {
      _ <- Future.unit
      tickersToPersist = CMCExternalTickerFetcher
        .convertCMCResponseToPersistence(
          tickers_
        )
      cnyTicker = ExternalTicker(
        "rmb",
        CMCExternalTickerFetcher
          .toDouble(BigDecimal(1) / BigDecimal(cnyToUsdRate))
      )
      now = timeProvider.getTimeSeconds()
      _ = tickers =
        tickersToPersist.+:(cnyTicker).map(t => t.copy(timestamp = now))
      fixGroup = tickersToPersist.grouped(20).toList
      _ <- Future.sequence(
        fixGroup.map(dbModule.externalTickerDal.saveTickers)
      )
      updateSucc <- dbModule.externalTickerDal.updateEffective(now)
    } yield {
      if (updateSucc != ErrorCode.ERR_NONE)
        log.error(s"CMC persist failed, code:$updateSucc")
      tickers
    }

}