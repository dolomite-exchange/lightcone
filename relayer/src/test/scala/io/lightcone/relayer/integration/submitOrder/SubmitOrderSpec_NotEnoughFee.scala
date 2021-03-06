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

package io.lightcone.relayer.integration.submitOrder

import io.lightcone.core.OrderStatus.STATUS_PENDING
import io.lightcone.core._
import io.lightcone.relayer.data.AccountBalance.TokenBalance
import io.lightcone.relayer.data._
import io.lightcone.relayer.getUniqueAccount
import io.lightcone.relayer.integration.AddedMatchers._
import io.lightcone.relayer.integration.Metadatas._
import io.lightcone.relayer.integration._
import org.scalatest._

class SubmitOrderSpec_NotEnoughFee
    extends FeatureSpec
    with GivenWhenThen
    with CommonHelper
    with ValidateHelper
    with Matchers {

  feature("submit  order ") {
    scenario("enough balance and enough allowance but not enough fee") {
      implicit val account = getUniqueAccount()
      Given(
        s"an new account with enough balance and enough allowance but not enough fee: ${account.getAddress}"
      )

      addAccountExpects({
        case req =>
          GetAccount.Res(
            Some(
              AccountBalance(
                address = req.address,
                tokenBalanceMap = req.tokens.map {
                  t =>
                    if (t == GTO_TOKEN.address) {
                      t -> AccountBalance.TokenBalance(
                        token = t,
                        balance = "10".zeros(GTO_TOKEN.decimals),
                        allowance = "10".zeros(GTO_TOKEN.decimals)
                      )
                    } else {
                      t -> AccountBalance.TokenBalance(
                        token = t,
                        balance = "1000".zeros(dynamicBaseToken.getDecimals()),
                        allowance = "1000".zeros(dynamicBaseToken.getDecimals())
                      )
                    }
                }.toMap
              )
            )
          )
      })

      val getBalanceReq = GetAccount.Req(
        account.getAddress,
        tokens = Seq(dynamicBaseToken.getAddress(), GTO_TOKEN.address)
      )
      getBalanceReq.expectUntil(
        check((res: GetAccount.Res) => res.accountBalance.nonEmpty)
      )

      When("submit an order.")

      val order = createRawOrder(
        tokenS = dynamicBaseToken.getAddress(),
        tokenB = dynamicQuoteToken.getAddress(),
        tokenFee = GTO_TOKEN.address,
        amountFee = "20".zeros(GTO_TOKEN.decimals)
      )
      SubmitOrder
        .Req(Some(order))
        .expect(check((res: SubmitOrder.Res) => res.success))

      Then(
        "submit order successfully"
      )

      And("the status of the order just submitted is status pending")
      And(
        "balance and allowance is 1000, available balance and available allowance is 990"
      )
      And("sell amount of order book is 5")

      defaultValidate(
        getOrdersMatcher = containsInGetOrders(STATUS_PENDING, order.hash),
        accountMatcher = accountBalanceMatcher(
          dynamicBaseToken.getAddress(),
          TokenBalance(
            token = dynamicBaseToken.getAddress(),
            balance = "1000".zeros(dynamicBaseToken.getMetadata.decimals),
            allowance = "1000".zeros(dynamicBaseToken.getMetadata.decimals),
            availableBalance =
              "990".zeros(dynamicBaseToken.getMetadata.decimals),
            availableAllowance =
              "990".zeros(dynamicBaseToken.getMetadata.decimals)
          )
        ),
        marketMatchers = Map(
          dynamicMarketPair -> (check(
            (res: GetOrderbook.Res) =>
              res.getOrderbook.sells.map(_.amount.toDouble).sum == 5
          ), defaultMatcher, defaultMatcher)
        )
      )

    }
  }

}
