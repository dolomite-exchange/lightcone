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

package io.lightcone.persistence.dals

import com.google.inject.Inject
import com.google.inject.name.Named
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import io.lightcone.lib._
import io.lightcone.core._
import io.lightcone.ethereum.event.BlockEvent
import io.lightcone.persistence._
import io.lightcone.ethereum.persistence._
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.JdbcProfile
import slick.basic._
import slick.lifted.Query
import scala.concurrent._
import scala.util.{Failure, Success}

class FillDalImpl @Inject()(
    implicit
    val ec: ExecutionContext,
    @Named("dbconfig-dal-fill") val dbConfig: DatabaseConfig[JdbcProfile],
    timeProvider: TimeProvider)
    extends FillDal {

  val query = TableQuery[FillTable]

  def saveFill(fill: Fill): Future[ErrorCode] = {
    db.run(
        (query += fill.copy(
          marketHash =
            MarketHash(MarketPair(fill.tokenS, fill.tokenB)).hashString(),
          sequenceId = 0L
        )).asTry
      )
      .map {
        case Failure(e: MySQLIntegrityConstraintViolationException) =>
          ErrorCode.ERR_PERSISTENCE_DUPLICATE_INSERT
        case Failure(ex) => {
          logger.error(s"error : ${ex.getMessage}")
          ErrorCode.ERR_PERSISTENCE_INTERNAL
        }
        case Success(x) => ErrorCode.ERR_NONE
      }
  }

  def saveFills(fills: Seq[Fill]): Future[Seq[ErrorCode]] =
    Future.sequence(fills.map(saveFill))

  def getFills(
      ownerOpt: Option[String],
      txHashOpt: Option[String],
      orderHashOpt: Option[String],
      ringHashOpt: Option[String],
      ringIndexOpt: Option[Long],
      fillIndexOpt: Option[Int],
      tokensOpt: Option[String],
      tokenbOpt: Option[String],
      marketHashOpt: Option[String],
      walletOpt: Option[String],
      minerOpt: Option[String],
      sort: SortingType,
      pagingOpt: Option[CursorPaging]
    ): Future[Seq[Fill]] = {
    var filters = queryFilters(
      ownerOpt,
      txHashOpt,
      orderHashOpt,
      ringHashOpt,
      ringIndexOpt,
      fillIndexOpt,
      tokensOpt,
      tokenbOpt,
      marketHashOpt,
      walletOpt,
      minerOpt
    )
    if (pagingOpt.nonEmpty) {
      val paging = pagingOpt.get
      filters = sort match {
        case SortingType.DESC =>
          if (paging.cursor > 0) {
            filters
              .filter(_.sequenceId < paging.cursor)
              .sortBy(_.sequenceId.desc)
          } else { // query latest
            filters.sortBy(_.sequenceId.desc)
          }
        case _ =>
          if (paging.cursor > 0) {
            filters
              .filter(_.sequenceId > paging.cursor)
              .sortBy(_.sequenceId.asc)
          } else {
            filters
              .sortBy(_.sequenceId.asc)
          }
      }
      filters = filters.take(paging.size)
    }
    db.run(filters.result)
  }

  def countFills(
      ownerOpt: Option[String],
      txHashOpt: Option[String],
      orderHashOpt: Option[String],
      ringHashOpt: Option[String],
      ringIndexOpt: Option[Long],
      fillIndexOpt: Option[Int],
      tokensOpt: Option[String],
      tokenbOpt: Option[String],
      marketHashOpt: Option[String],
      walletOpt: Option[String],
      minerOpt: Option[String]
    ): Future[Int] = {
    val filters = queryFilters(
      ownerOpt,
      txHashOpt,
      orderHashOpt,
      ringHashOpt,
      ringIndexOpt,
      fillIndexOpt,
      tokensOpt,
      tokenbOpt,
      marketHashOpt,
      walletOpt,
      minerOpt
    )
    db.run(filters.size.result)
  }

  def getMarketFills(
      marketPair: MarketPair,
      num: Int
    ): Future[Seq[Fill]] = {
    db.run(
      query
        .filter(_.marketHash === MarketHash(marketPair).hashString())
        .filter(_.isTaker === true)
        .sortBy(_.blockHeight.desc)
        .take(num)
        .result
    )
  }

  def cleanActivitiesForReorg(req: BlockEvent): Future[Int] =
    db.run(
      query
        .filter(_.blockHeight >= req.blockNumber)
        .delete
    )

  private def queryFilters(
      owner: Option[String] = None,
      txHash: Option[String] = None,
      orderHash: Option[String] = None,
      ringHash: Option[String] = None,
      ringIndex: Option[Long] = None,
      fillIndex: Option[Int] = None,
      tokenS: Option[String] = None,
      tokenB: Option[String] = None,
      marketHashOpt: Option[String] = None,
      wallet: Option[String] = None,
      miner: Option[String] = None
    ): Query[FillTable, FillTable#TableElementType, Seq] = {
    var filters = query.filter(_.ringIndex >= 0L)
    if (owner.nonEmpty) filters = filters.filter(_.owner === owner.get)
    if (txHash.nonEmpty) filters = filters.filter(_.txHash === txHash.get)
    if (orderHash.nonEmpty)
      filters = filters.filter(_.orderHash === orderHash.get)
    if (ringHash.nonEmpty) filters = filters.filter(_.ringHash === ringHash.get)
    if (ringIndex.nonEmpty)
      filters = filters.filter(_.ringIndex === ringIndex.get)
    if (fillIndex.nonEmpty)
      filters = filters.filter(_.fillIndex === fillIndex.get)
    if (tokenS.nonEmpty) filters = filters.filter(_.tokenS === tokenS.get)
    if (tokenB.nonEmpty) filters = filters.filter(_.tokenB === tokenB.get)
    if (marketHashOpt.nonEmpty)
      filters = filters.filter(_.marketHash === marketHashOpt.get)
    if (wallet.nonEmpty) filters = filters.filter(_.wallet === wallet.get)
    if (miner.nonEmpty) filters = filters.filter(_.miner === miner.get)
    filters
  }
}
