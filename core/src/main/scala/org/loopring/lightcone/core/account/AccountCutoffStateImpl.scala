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

package org.loopring.lightcone.core.account

import org.loopring.lightcone.lib.{ErrorException, TimeProvider}
import org.loopring.lightcone.proto.ErrorCode._
import org.loopring.lightcone.proto._
import org.web3j.utils.Numeric

import scala.collection.mutable.Map

class AccountCutoffStateImpl()(implicit timeProvider: TimeProvider)
    extends AccountCutoffState {
  private val marketPairCutoffs = Map.empty[BigInt, Long]
  private var ownerCutoff: Long = -1L

  def setCutoff(
      marketId: MarketId,
      cutoff: Long
    ) = {
    if (!(cutoff <= timeProvider.getTimeSeconds())) {
      val marketCode = Numeric.toBigInt(marketId.primary) xor
        Numeric.toBigInt(marketId.secondary)
      marketPairCutoffs.get(marketCode) match {
        case None => marketPairCutoffs.put(marketCode, cutoff)
        case Some(c) =>
          if (c < cutoff)
            marketPairCutoffs.put(marketCode, cutoff)
      }
    }
  }

  def setCutoff(cutoff: Long) = {
    if (!(cutoff <= timeProvider.getTimeSeconds()))
      if (ownerCutoff < cutoff) ownerCutoff = cutoff
  }

  def isOrderCutoff(rawOrder: RawOrder) = {
    if (ownerCutoff >= rawOrder.validSince) {
      throw ErrorException(
        ERR_ORDER_VALIDATION_INVALID_CUTOFF,
        s"this address has been set cutoff=$ownerCutoff."
      )
    }
    val marketCode = Numeric.toBigInt(rawOrder.tokenS) xor Numeric.toBigInt(
      rawOrder.tokenB
    )
    if (marketPairCutoffs.contains(marketCode) &&
        marketPairCutoffs(marketCode) > rawOrder.validSince) {
      throw ErrorException(
        ERR_ORDER_VALIDATION_INVALID_CUTOFF,
        s"the market ${rawOrder.tokenS}-${rawOrder.tokenB} " +
          s"of this address has been set cutoff=$ownerCutoff."
      )
    }
  }
}