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

import io.lightcone.ethereum.TxStatus
import io.lightcone.lib.Address
import io.lightcone.lib.NumericConversion._
import io.lightcone.relayer._
import io.lightcone.relayer.actors.ActivityActor
import io.lightcone.relayer.data.{GetAccount, GetActivities}
import io.lightcone.relayer.integration.AddedMatchers._
import io.lightcone.relayer.integration.Metadatas._
import io.lightcone.relayer.integration.helper.{AccountHelper, ActivityHelper}
import org.scalatest._

class TransferERC20Spec_failed
    extends FeatureSpec
    with GivenWhenThen
    with CommonHelper
    with AccountHelper
    with ActivityHelper
    with Matchers {

  feature("transfer ERC20 failed") {
    scenario("transfer ERC20") {
      implicit val account = getUniqueAccount()
      val txHash =
        "0xbc6331920f91aa6f40e10c3e6c87e6d58aec01acb6e9a244983881d69bc0cff4"
      val to = getUniqueAccount()
      val blockNumber = 987L
      val nonce = 11L

      Given("initialize balance")
      mockAccountWithFixedBalance(account.getAddress, dynamicMarketPair)
      mockAccountWithFixedBalance(to.getAddress, dynamicMarketPair)

      Then("check initialize balance")
      val getFromAddressBalanceReq = GetAccount.Req(
        account.getAddress,
        allTokens = true
      )
      val getToAddressBalanceReq = GetAccount.Req(
        to.getAddress,
        allTokens = true
      )
      getFromAddressBalanceReq.expectUntil(
        check((res: GetAccount.Res) => {
          val balanceOpt = res.accountBalance
          val ethBalance = toBigInt(
            balanceOpt.get.tokenBalanceMap(Address.ZERO.toString).balance.get
          )
          val ethAvailableBalance = toBigInt(
            balanceOpt.get
              .tokenBalanceMap(Address.ZERO.toString)
              .availableBalance
              .get
          )
          ethBalance == "20".zeros(18) && ethBalance == ethAvailableBalance
        })
      )
      getToAddressBalanceReq.expectUntil(
        check((res: GetAccount.Res) => {
          val balanceOpt = res.accountBalance
          val ethBalance = toBigInt(
            balanceOpt.get.tokenBalanceMap(Address.ZERO.toString).balance.get
          )
          val ethAvailableBalance = toBigInt(
            balanceOpt.get
              .tokenBalanceMap(Address.ZERO.toString)
              .availableBalance
              .get
          )
          ethBalance == "20".zeros(18) && ethBalance == ethAvailableBalance
        })
      )

      When("send some transfer events")
      tokenTransferPendingActivities(
        account.getAddress,
        to.getAddress,
        blockNumber,
        txHash,
        LRC_TOKEN.address,
        "100".zeros(18),
        nonce
      ).foreach(eventDispatcher.dispatch)
      Thread.sleep(1000)

      Then("the each account should query one pending activity")
      GetActivities
        .Req(account.getAddress)
        .expectUntil(
          check((res: GetActivities.Res) => {
            res.activities.length == 1 && res.activities.head.txStatus == TxStatus.TX_STATUS_PENDING
          })
        )
      GetActivities
        .Req(to.getAddress)
        .expectUntil(
          check((res: GetActivities.Res) => {
            res.activities.length == 1 && res.activities.head.txStatus == TxStatus.TX_STATUS_PENDING
          })
        )

      When("activities confirmed")
      val blockEvent =
        blockConfirmedEvent(account.getAddress, blockNumber, txHash, nonce)
      ActivityActor.broadcast(blockEvent)
      Thread.sleep(2000)

      tokenTransferFailedActivities(
        account.getAddress,
        to.getAddress,
        blockNumber,
        txHash,
        LRC_TOKEN.address,
        "100".zeros(18),
        nonce
      ).foreach(eventDispatcher.dispatch)
      Thread.sleep(1000)

      GetActivities
        .Req(account.getAddress)
        .expectUntil(
          check((res: GetActivities.Res) => {
            res.activities.length == 1 && res.activities.head.txStatus == TxStatus.TX_STATUS_FAILED
          })
        )
      GetActivities
        .Req(to.getAddress)
        .expectUntil(
          check((res: GetActivities.Res) => {
            res.activities.length == 1 && res.activities.head.txStatus == TxStatus.TX_STATUS_FAILED
          })
        )

      getFromAddressBalanceReq.expectUntil(
        check((res: GetAccount.Res) => {
          val balanceOpt = res.accountBalance
          val ethBalance = toBigInt(
            balanceOpt.get.tokenBalanceMap(Address.ZERO.toString).balance.get
          )
          val ethAvailableBalance = toBigInt(
            balanceOpt.get
              .tokenBalanceMap(Address.ZERO.toString)
              .availableBalance
              .get
          )
          val lrcBalance = toBigInt(
            balanceOpt.get.tokenBalanceMap(LRC_TOKEN.address).balance.get
          )
          val lrcAvailableBalance = toBigInt(
            balanceOpt.get
              .tokenBalanceMap(LRC_TOKEN.address)
              .availableBalance
              .get
          )
          ethBalance == "20"
            .zeros(18) && ethBalance == ethAvailableBalance && lrcBalance == "4000"
            .zeros(18) && lrcBalance == lrcAvailableBalance
        })
      )
      getToAddressBalanceReq.expectUntil(
        check((res: GetAccount.Res) => {
          val balanceOpt = res.accountBalance
          val ethBalance = toBigInt(
            balanceOpt.get.tokenBalanceMap(Address.ZERO.toString).balance.get
          )
          val ethAvailableBalance = toBigInt(
            balanceOpt.get
              .tokenBalanceMap(Address.ZERO.toString)
              .availableBalance
              .get
          )
          val lrcBalance = toBigInt(
            balanceOpt.get.tokenBalanceMap(LRC_TOKEN.address).balance.get
          )
          val lrcAvailableBalance = toBigInt(
            balanceOpt.get
              .tokenBalanceMap(LRC_TOKEN.address)
              .availableBalance
              .get
          )
          ethBalance == "20"
            .zeros(18) && ethBalance == ethAvailableBalance && lrcBalance == "4000"
            .zeros(18) && lrcBalance == lrcAvailableBalance
        })
      )
    }
  }
}
