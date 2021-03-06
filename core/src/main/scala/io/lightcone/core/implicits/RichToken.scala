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

package io.lightcone.core

import spire.math.Rational

private[core] class RichToken(token: Token) {
  val scaling = Rational(BigInt(10).pow(token.metadata.get.decimals))

  def fromWei(amount: BigInt): Double =
    (Rational(amount) / scaling).doubleValue

  def fromWei(
      amount: BigInt,
      precision: Int
    ): Double = {
    BigDecimal(fromWei(amount))
      .setScale(precision, BigDecimal.RoundingMode.HALF_UP)
      .doubleValue
  }

  def toWei(amount: Double): BigInt =
    (Rational(amount) * scaling).toBigInt

  def getSymbol() = {
    token.metadata
      .getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found metadata of token $token"
        )
      )
      .symbol
  }

  def getAddress() = {
    token.metadata
      .getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found metadata of token $token"
        )
      )
      .address
  }

  def getDecimals() = {
    token.metadata
      .getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found metadata of token $token"
        )
      )
      .decimals
  }

  def getBurnRateForMarket() = {
    token.metadata
      .getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found metadata of token $token"
        )
      )
      .burnRate
      .getOrElse(BurnRate())
      .forMarket
  }

  def getBurnRateForP2P() = {
    token.metadata
      .getOrElse(
        throw ErrorException(
          ErrorCode.ERR_INTERNAL_UNKNOWN,
          s"not found metadata of token $token"
        )
      )
      .burnRate
      .getOrElse(BurnRate())
      .forP2P
  }
}
