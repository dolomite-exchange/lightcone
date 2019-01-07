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

package org.loopring.lightcone.ethereum.event

import org.loopring.lightcone.ethereum.abi.TokenTierUpgradedEvent
import org.loopring.lightcone.ethereum.data.Address
import org.loopring.lightcone.proto._

class TokenBurnRateEventExtractor(
    rateMap: Map[String, Int],
    base: Int)
    extends DataExtractor[TokenBurnRateChangedEvent] {

  def extract(
      tx: Transaction,
      receipt: TransactionReceipt,
      blockTime: String
    ): Seq[TokenBurnRateChangedEvent] = {
    receipt.logs.zipWithIndex.map { log =>
      loopringProtocolAbi.unpackEvent(log._1.data, log._1.topics.toArray) match {
        case Some(event: TokenTierUpgradedEvent.Result) =>
          Some(
            TokenBurnRateChangedEvent(
              header = Some(
                getEventHeader(tx, receipt, blockTime)
                  .withLogIndex(log._2)
              ),
              token = event.add,
              burnRate = rateMap(Address(event.add).toString) / base.toDouble
            )
          )
        case _ =>
          None
      }
    }.filter(_.nonEmpty).map(_.get)
  }
}
