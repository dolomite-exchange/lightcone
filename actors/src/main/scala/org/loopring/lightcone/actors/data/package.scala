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

package org.loopring.lightcone.actors

import org.loopring.lightcone.core.data.{ Order, OrderState }
import org.loopring.lightcone.proto.actors._
import com.google.protobuf.ByteString

package object data {

  case class BalanceAndAllowance(balance: BigInt, allowance: BigInt)

  ///////////

  implicit def byteString2BigInt(bytes: ByteString): BigInt = {
    if (bytes.size() > 0)
      BigInt(bytes.toByteArray)
    else BigInt(0)
  }
  implicit def bigInt2ByteString(b: BigInt): ByteString = ByteString.copyFrom(b.toByteArray)

  implicit def xBalanceAndAlowance2BalanceAndAlowance(xba: XBalanceAndAllowance): BalanceAndAllowance = {
    BalanceAndAllowance(
      balance = xba.balance,
      allowance = xba.allowance
    )
  }
  implicit def balanceAndAlowance2XBalanceAndAlowance(ba: BalanceAndAllowance): XBalanceAndAllowance = {
    XBalanceAndAllowance(
      balance = ba.balance,
      allowance = ba.allowance
    )
  }

  implicit def xOrder2Order(xorder: XOrder): Order = {
    Order(
      id = xorder.id,
      tokenS = xorder.tokenS,
      tokenB = xorder.tokenB,
      tokenFee = xorder.tokenFee,
      amountS = xorder.amountS,
      amountB = xorder.amountB,
      amountFee = xorder.amountFee,
      createdAt = xorder.createdAt,
      status = xorder.status,
      walletSplitPercentage = xorder.walletSplitPercentage,
      _outstanding = Some(xorder.getOutstanding),
      _reserved = Some(xorder.getReserved),
      _actual = Some(xorder.getActual),
      _matchable = Some(xorder.getMatchable)
    )
  }
  implicit def order2XOrder(order: Order): XOrder = {
    XOrder(
      id = order.id,
      tokenS = order.tokenS,
      tokenB = order.tokenB,
      tokenFee = order.tokenFee,
      amountS = order.amountS,
      amountB = order.amountB,
      amountFee = order.amountFee,
      createdAt = order.createdAt,
      status = order.status,
      walletSplitPercentage = order.walletSplitPercentage,
      outstanding = Some(order.outstanding),
      reserved = Some(order.reserved),
      actual = Some(order.actual),
      matchable = Some(order.matchable)
    )
  }

  implicit def XOrderStateToOrderState(xOrderState: XOrderState): OrderState = {
    OrderState(
      amountS = xOrderState.amountS,
      amountB = xOrderState.amountB,
      amountFee = xOrderState.amountFee
    )
  }
  implicit def OrderStateToXOrderState(orderState: OrderState): XOrderState = {
    XOrderState(
      amountS = orderState.amountS,
      amountB = orderState.amountB,
      amountFee = orderState.amountFee
    )
  }

  implicit def byteArray2ByteString(bytes: Array[Byte]) = ByteString.copyFrom(bytes)
}
