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

package io.lightcone.relayer.integration
import io.lightcone.relayer._
import io.lightcone.relayer.data.{GetAccount, GetOrderbook, SubmitOrder}
import io.lightcone.relayer.integration.AddedMatchers._
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._

class RpcHelperSpec
    extends FeatureSpec
    with GivenWhenThen
    with CommonHelper
    with Matchers {

  feature("an example of RpcHelper") {
    scenario("submit one order") {
      implicit val account = getUniqueAccount()

      Given("an account with Balance")
      val getBalanceReq = GetAccount.Req(
        account.getAddress,
        tokens = Seq(
          dynamicBaseToken.getMetadata.address,
          dynamicQuoteToken.getMetadata.address
        )
      )

      val res = getBalanceReq.expectUntil(
        check((res: GetAccount.Res) => res.accountBalance.nonEmpty)
      )
      When("submit an order.")
      val order = createRawOrder(
        tokenS = dynamicBaseToken.getMetadata.address,
        tokenB = dynamicQuoteToken.getMetadata.address,
        tokenFee = dynamicBaseToken.getMetadata.address
      )
      val submitRes = SubmitOrder
        .Req(Some(order))
        .expect(check((res: SubmitOrder.Res) => true))
      info(s"the result of submit order is ${submitRes.success}")

      Then("the orderbook should not be empty.")
      val orderbook = GetOrderbook
        .Req(
          0,
          100,
          Some(dynamicMarketPair)
        )
        .expect(
          check((res: GetOrderbook.Res) => res.orderbook.get.sells.nonEmpty)
        )
      info(s"the orderbook is ${orderbook}")
    }
  }
}
