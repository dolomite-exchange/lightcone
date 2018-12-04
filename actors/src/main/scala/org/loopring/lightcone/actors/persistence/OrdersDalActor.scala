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

package org.loopring.lightcone.actors.persistence

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern._
import akka.util.Timeout
import org.loopring.lightcone.persistence.dals._
import org.loopring.lightcone.proto.actors._
import org.loopring.lightcone.proto.core._
import org.loopring.lightcone.proto.persistence._

import scala.concurrent._

object OrdersDalActor {
  val name = "orders_dal"
}

class OrdersDalActor(
    ordersDal: OrderDal
)(
    implicit
    ec: ExecutionContext,
    timeout: Timeout
)
  extends Actor
  with ActorLogging {

  def receive: Receive = LoggingReceive {
    case XRecoverOrdersReq(address, marketIdOpt, updatedSince, num) ⇒
      val tokenes = (marketIdOpt match {
        case Some(marketId) ⇒ Set(marketId.primary, marketId.secondary)
        case None           ⇒ Seq.empty[String]
      }).toSet
      (for {
        orders ← ordersDal.getOrdersByUpdatedAt(
          num = num,
          statuses = Set(XOrderStatus.STATUS_NEW, XOrderStatus.STATUS_PENDING),
          tokenSSet = tokenes,
          tokenBSet = tokenes,
          owners = if ("" != address) Set(address) else Set.empty,
          updatedSince = Some(updatedSince)
        )
      } yield XRecoverOrdersRes(orders)) pipeTo sender

    case XSaveOrderReq(Some(xraworder)) ⇒
      ordersDal.saveOrder(xraworder) pipeTo sender

    case XUpdateOrderStateReq(hash, stateOpt, changeUpdatedAtField) ⇒
      (stateOpt match {
        case Some(state) ⇒ ordersDal.updateOrderState(hash, state, changeUpdatedAtField)
        case None        ⇒ Future.successful(Left(XPersistenceError.PERS_ERR_INVALID_DATA))
      }) pipeTo sender

    case XUpdateOrderStatusReq(hash, status, changeUpdatedAtField) ⇒
      ordersDal.updateOrderStatus(hash, status, changeUpdatedAtField) pipeTo sender

    case XGetOrdersByHashesReq(hashes) ⇒
      (for {
        orders ← ordersDal.getOrders(hashes)
      } yield XGetOrdersByHashesRes(orders)) pipeTo sender
    case XGetOrderByHashReq(hash) ⇒
      (for {
        order ← ordersDal.getOrder(hash)
      } yield XGetOrderByHashRes(order)) pipeTo sender

  }
}
