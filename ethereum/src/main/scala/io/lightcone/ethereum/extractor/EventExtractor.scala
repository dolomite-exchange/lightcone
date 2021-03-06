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

package io.lightcone.ethereum.extractor

import io.lightcone.ethereum.TxStatus
import io.lightcone.ethereum.persistence.{Activity, Fill, TxEvents}
import io.lightcone.lib.{Address, NumericConversion}
import org.slf4s.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait EventExtractor[S, +E] {
  def extractEvents(source: S): Future[Seq[E]]
}

object EventExtractor extends Object with Logging {

  // Constructs a new EventExtractor with a given list of sub-EventExtractors and
  // invokes them in the given order. Duplicatd events will be removed.
  def compose[S, E](
      extractors: EventExtractor[S, E]*
    )(
      implicit
      ec: ExecutionContext
    ) = new EventExtractor[S, E] {

    def extractEvents(source: S): Future[Seq[E]] = {

      Future
        .sequence(extractors.map(extractAndLog(_, source)))
        .map(_.flatten.distinct)
    }

    private def extractAndLog(
        extractor: EventExtractor[S, E],
        source: S
      ): Future[Seq[E]] = {
      val f = extractor.extractEvents(source)

      f.onComplete {
        case Success(events) =>
          val size = events.size
          log.debug(
            s"EXTRACTOR ${extractor.getClass.getSimpleName} extracted " +
              s"$size events from: ${source.toString.take(1000)} ===>"
          )

          events.zipWithIndex foreach {
            case (evt, idx) => log.debug(s"EVENT ${idx}/${size} --> ${evt}")
          }

        case Failure(e) =>
          log.error(
            s"${extractor.getClass.getSimpleName} failed to extract events from $source",
            e
          )
      }

      f
    }
  }

  def extractDefaultActivity(txData: TransactionData) = {
    val activity = Activity(
      owner = txData.tx.from,
      block = txData.receiptAndHeaderOpt
        .map(_._1.getBlockNumber)
        .map(NumericConversion.toBigInt)
        .map(_.longValue())
        .getOrElse(-1L),
      txHash = txData.tx.hash,
      timestamp = txData.receiptAndHeaderOpt
        .map(_._2.getBlockHeader.timestamp)
        .getOrElse(0L),
      token = Address.ZERO.toString(),
      from = txData.tx.from,
      nonce = NumericConversion.toBigInt(txData.tx.getNonce).longValue(),
      txStatus = txData.receiptAndHeaderOpt
        .map(_._1.status)
        .getOrElse(TxStatus.TX_STATUS_PENDING),
      detail = Activity.Detail.EtherTransfer(
        Activity.EtherTransfer(
          address = txData.tx.to,
          amount = txData.tx.value
        )
      )
    )

    Seq(
      activity.copy(
        owner = txData.tx.from,
        activityType = Activity.ActivityType.ETHER_TRANSFER_OUT
      ),
      activity.copy(
        owner = txData.tx.to,
        activityType = Activity.ActivityType.ETHER_TRANSFER_IN
      )
    )
  }

  def composeActivities(activities: Seq[Activity]): Seq[TxEvents] = {
    activities
      .groupBy(_.txHash)
      .values
      .map(
        activities =>
          TxEvents(
            TxEvents.Events.Activities(
              TxEvents.Activities(activities)
            )
          )
      )
      .toSeq
  }

  def composeFills(fills: Seq[Fill]): Seq[TxEvents] = {
    fills
      .groupBy(_.txHash)
      .values
      .map(
        fills =>
          TxEvents(
            TxEvents.Events.Fills(
              TxEvents.Fills(fills)
            )
          )
      )
      .toSeq
  }

}
