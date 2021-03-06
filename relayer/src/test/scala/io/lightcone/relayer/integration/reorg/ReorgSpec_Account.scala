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

import io.lightcone.core.Amount
import io.lightcone.ethereum.event._
import io.lightcone.lib.NumericConversion._
import io.lightcone.relayer._
import io.lightcone.relayer.data._
import io.lightcone.relayer.integration.AddedMatchers._
import io.lightcone.relayer.integration.Metadatas._
import org.scalatest._
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.math.BigInt

class ReorgSpec_Account
    extends FeatureSpec
    with GivenWhenThen
    with CommonHelper
    with CancelHelper
    with ValidateHelper
    with Matchers {

  feature("test reorg of account ") {
    scenario("the value of account's balance and allowance after forked") {
      implicit val account = getUniqueAccount()
      val blockNumber = 1000L
      val token = LRC_TOKEN.address
      val balance: Option[Amount] = BigInt(100000)
      val getAccountReq = GetAccount.Req(
        address = account.getAddress,
        allTokens = true
      )
      val accountInitRes = getAccountReq.expectUntil(
        check((res: GetAccount.Res) => res.accountBalance.nonEmpty)
      )

      val initAllowance: BigInt = accountInitRes.getAccountBalance
        .tokenBalanceMap(token)
        .allowance

      Given("dispatch an AddressBalanceUpdatedEvent.")
      val balanceEvent = AddressBalanceUpdatedEvent(
        address = account.getAddress,
        token = token,
        balance = balance,
        block = blockNumber
      )
      addAccountExpects({
        case req: GetAccount.Req =>
          GetAccount.Res(
            Some(
              AccountBalance(
                address = req.address,
                tokenBalanceMap = req.tokens.map { t =>
                  t -> AccountBalance.TokenBalance(
                    token = t,
                    balance = balance,
                    allowance = accountInitRes.getAccountBalance
                      .tokenBalanceMap(token)
                      .allowance
                  )
                }.toMap
              )
            )
          )
      })
      eventDispatcher.dispatch(balanceEvent)

      getAccountReq.expectUntil(
        Matcher { res: GetAccount.Res =>
          MatchResult(
            res.accountBalance.nonEmpty,
            "accountBalance is empty",
            "accountBalance is nonEmpty"
          )
        } and Matcher { res: GetAccount.Res =>
          val balance1: BigInt = res.getAccountBalance
            .tokenBalanceMap(token)
            .balance
          MatchResult(
            balance1 == toBigInt(balance),
            s"balance: ${balance1} != ${toBigInt(balance)}",
            s"balance: ${balance1} == ${toBigInt(balance)}"
          )
        }
      )
      Given("dispatch an AddressAllowanceUpdatedEvent.")
      val allowanceEvent = AddressAllowanceUpdatedEvent(
        address = account.getAddress,
        token = token,
        allowance = balance,
        block = blockNumber
      )
      addAccountExpects({
        case req: GetAccount.Req =>
          GetAccount.Res(
            Some(
              AccountBalance(
                address = req.address,
                tokenBalanceMap = req.tokens.map { t =>
                  t -> AccountBalance.TokenBalance(
                    token = t,
                    balance = balance,
                    allowance = balance
                  )
                }.toMap
              )
            )
          )
      })
      eventDispatcher.dispatch(allowanceEvent)
      Then("the balance and allowance should have changed.")
      val accountRes = getAccountReq.expectUntil(
        check(
          (res: GetAccount.Res) =>
            res.accountBalance.nonEmpty &&
              res.getAccountBalance.tokenBalanceMap(token).allowance == balance
        )
      )
      accountRes.getAccountBalance.tokenBalanceMap(token).allowance should be(
        balance
      )
      accountRes.getAccountBalance.tokenBalanceMap(token).balance should be(
        balance
      )
      When("dispatch a forked block event.")
      val blockEvent = BlockEvent(
        blockNumber = blockNumber - 1
      )
      addAccountExpects({
        case req: GetAccount.Req =>
          GetAccount.Res(
            Some(
              AccountBalance(
                address = req.address,
                tokenBalanceMap = req.tokens.map { t =>
                  t -> AccountBalance.TokenBalance(
                    token = t,
                    balance = accountInitRes.getAccountBalance
                      .tokenBalanceMap(token)
                      .balance,
                    allowance = accountInitRes.getAccountBalance
                      .tokenBalanceMap(token)
                      .allowance
                  )
                }.toMap
              )
            )
          )
      })

      eventDispatcher.dispatch(blockEvent)
      Thread.sleep(1000)

      Then("the balance and allowance should have rollbacked.")
      val accountRes1 = getAccountReq.expectUntil(
        Matcher { res: GetAccount.Res =>
          MatchResult(
            res.accountBalance.nonEmpty,
            "accountBalance is empty",
            "accountBalance is nonEmpty"
          )
        } and Matcher { res: GetAccount.Res =>
          val allowance1: BigInt = res.getAccountBalance
            .tokenBalanceMap(token)
            .allowance
          MatchResult(
            allowance1 == initAllowance,
            s"allowance: ${allowance1} != ${initAllowance}",
            s"allowance: ${allowance1} == ${initAllowance}"
          )
        }
      )
    }
  }
}
