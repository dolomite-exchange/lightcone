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

import io.lightcone.lib._
import scala.concurrent._
import io.lightcone.core.testing._

abstract class AccountManagerImplSpec extends CommonSpec {

  var owner = "owning_address"
  // TODO(dongw): add more tests to verify these two numbers.
  var numOfOrdersProcessed = 0
  var numOfAccountsProcessed = 0

  var balanceRefreshIntervalSeconds = 1000

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit val timeProvider: TimeProvider = new SystemTimeProvider
  implicit val balanceProvider = stub[BalanceAndAllowanceProvider]

  implicit val updatdOrdersProcessor = new UpdatedOrdersProcessor()(ec) {

    def processUpdatedOrder(
        trackOrderUpdated: Boolean,
        order: Matchable
      ): Future[Any] = {
      numOfOrdersProcessed += 1
      processOneOrder(order)
      Future.unit
    }
  }

  implicit val updatdAccountsProcessor = new UpdatedAccountsProcessor {

    def processUpdatedAccount(
        block: Long,
        address: String,
        tokenAddress: String
      ) = {
      numOfAccountsProcessed += 1
      Future.unit
    }
  }

  var manager: AccountManager = _

  var processOneOrder: Matchable => Unit = { order =>
    // println(s"==> order: $order")
  }

  override def beforeEach(): Unit = {
    numOfOrdersProcessed = 0
    numOfAccountsProcessed = 0

    manager = AccountManager.default(owner, balanceRefreshIntervalSeconds, true)
  }

  def setBalanceAllowance(
      block: Long,
      owner: String,
      token: String,
      balance: BigInt,
      allowance: BigInt
    ) =
    (balanceProvider.getBalanceAndALlowance _)
      .when(owner, token)
      .returns(Future.successful((block, balance, allowance)))
      .once

  def setSpendable(
      block: Long,
      owner: String,
      token: String,
      spendable: BigInt
    ) = setBalanceAllowance(block, owner, token, spendable, spendable)

  def submitSingleOrderExpectingSuccess(
      order: Matchable
    )(genExpectedOrder: Matchable => Matchable
    ): Matchable = {
    val (success, orderMap) = manager.resubmitOrder(order).await
    success should be(true)
    orderMap.size should be(1)
    orderMap(order.id) should be(genExpectedOrder(order))
    orderMap(order.id)
  }

  def submitSingleOrderExpectingFailure(
      order: Matchable
    )(genExpectedOrder: Matchable => Matchable
    ): Matchable = {
    val (success, orderMap) = manager.resubmitOrder(order).await
    success should be(false)
    orderMap.size should be(1)
    orderMap(order.id) should be(genExpectedOrder(order))
    orderMap(order.id)
  }

  def softCancelSingleOrderExpectingSuccess(
      orderId: String
    )(expectdOrder: Matchable
    ): Matchable = {
    val (success, orderMap) = manager.cancelOrder(orderId).await
    success should be(true)
    orderMap.size should be(1)
    orderMap(orderId) should be(expectdOrder)
    orderMap(orderId)
  }

  def hardCancelSingleOrderExpectingSuccess(
      block: Long,
      orderId: String
    )(expectdOrder: Matchable
    ): Matchable = {
    val orderMap = manager.hardCancelOrder(block, orderId).await
    orderMap.size should be(1)
    orderMap(orderId) should be(expectdOrder)
    orderMap(orderId)
  }

  def submitRandomOrder(maxSize: Int): Unit = {
    val r = rand.nextInt(TOKENS.size)
    val tokenS = TOKENS(r)
    val tokenB = TOKENS((r + 1) % TOKENS.size)
    val tokenFee = randomToken()

    val amountS: Double = (rand.nextInt(maxSize / 2) + maxSize / 2 + 1).toDouble
    val amountB: Double = (rand.nextInt(maxSize / 2) + maxSize / 2 + 1).toDouble
    val amountFee: Double = (rand.nextInt(maxSize / 2)).toDouble

    val order = owner |> (amountS ^ tokenS) --> (amountB ^ tokenB) -- (amountFee ^ tokenFee)
    manager.resubmitOrder(order).await
  }
}
