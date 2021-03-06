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

package io.lightcone.relayer.support

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import io.lightcone.relayer.RpcBinding
import io.lightcone.relayer.jsonrpc._
import io.lightcone.relayer.data._
import io.lightcone.core._
import io.lightcone.lib.ProtoSerializer
import org.slf4s.Logging
import scalapb.GeneratedMessage

import scala.concurrent.{Await, ExecutionContext}

trait HttpSupport extends RpcBinding with Logging {
  val config: Config
  implicit val materializer: ActorMaterializer

  // TODO:for test, not need it
  override val requestHandler: ActorRef = ActorRef.noSender

  val ps = new ProtoSerializer

  def singleRequest[T <: GeneratedMessage](
      req: T,
      method: String
    )(
      implicit
      system: ActorSystem,
      ec: ExecutionContext
    ) = {
    val json = req match {
      case m: scalapb.GeneratedMessage =>
        ps.serialize(m)
    }
    val reqJson = JsonRpcRequest("2.0", method, json, Some("1"))
    for {
      response <- Http().singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          entity = HttpEntity(
            ContentTypes.`application/json`,
            serialization.write(reqJson)
          ),
          uri = Uri(
            s"http://127.0.0.1:${config.getString("jsonrpc.http.port")}/" +
              s"${config.getString("jsonrpc.endpoint")}/${config.getString("jsonrpc.loopring")}"
          )
        )
      )
      res <- response.status match {
        case StatusCodes.OK =>
          response.entity.toStrict(timeout.duration).map { r =>
            val j = parse.parse(r.data.utf8String).extract[JsonRpcResponse]

            j.result match {
              case Some(r1) =>
                getReply(method).get
                  .jsonToExternalResponse(r1)
              case None =>
                j.error match {
                  case Some(err) =>
                    throw ErrorException(
                      ErrorCode.ERR_INTERNAL_UNKNOWN,
                      s"msg:${err}"
                    )
                  case None =>
                    throw ErrorException(
                      ErrorCode.ERR_INTERNAL_UNKNOWN,
                      s"res:${response}"
                    )
                }
            }
          }
        case _ =>
          throw ErrorException(
            ErrorCode.ERR_INTERNAL_UNKNOWN,
            s"res:${response}"
          )
      }
    } yield res
  }

  def expectOrderbookRes(
      req: GetOrderbook.Req,
      assertFun: Orderbook => Boolean,
      expectTimeout: Option[Timeout] = None
    ) = {
    var resOpt: Option[Orderbook] = None
    val timeout1 = if (expectTimeout.isEmpty) timeout else expectTimeout.get
    val lastTime = System.currentTimeMillis() + timeout1.duration.toMillis
    while (resOpt.isEmpty &&
           System.currentTimeMillis() <= lastTime) {
      val orderbookF = singleRequest(req, "get_orderbook")
      val orderbookRes = Await.result(orderbookF, timeout.duration)
      orderbookRes match {
        case GetOrderbook.Res(Some(orderbook)) =>
          if (assertFun(orderbook)) {
            resOpt = Some(orderbook)
          }
      }
      if (resOpt.isEmpty) {
        Thread.sleep(200)
      }
    }
    // if (resOpt.isEmpty) {
    //   throw new Exception(
    //     s"Timed out waiting for expectOrderbookRes of req:${req} "
    //   )
    // }
    resOpt
  }

  def expectBalanceRes(
      req: GetAccount.Req,
      assertFun: GetAccount.Res => Boolean,
      expectTimeout: Timeout = timeout
    ) = {
    var resOpt: Option[GetAccount.Res] = None
    val lastTime = System.currentTimeMillis() + timeout.duration.toMillis

    //必须等待jsonRpcServer启动完成
    while (resOpt.isEmpty &&
           System.currentTimeMillis() <= lastTime) {
      val getBalanceResF =
        singleRequest(req, "get_account")
      val res = Await.result(
        getBalanceResF.mapTo[GetAccount.Res],
        timeout.duration
      )
      if (assertFun(res)) {
        resOpt = Some(res)
      } else {
        Thread.sleep(200)
      }
    }
    if (resOpt.isEmpty) {
      throw new Exception(
        s"Timed out waiting for expectBalanceRes of req:${req} "
      )
    }
    resOpt
  }

  def expectTradeRes(
      req: GetUserFills.Req,
      assertFun: GetUserFills.Res => Boolean,
      expectTimeout: Timeout = timeout
    ) = {
    var resOpt: Option[GetUserFills.Res] = None
    val lastTime = System.currentTimeMillis() + timeout.duration.toMillis

    //必须等待jsonRpcServer启动完成
    while (resOpt.isEmpty &&
           System.currentTimeMillis() <= lastTime) {
      val getFillsF =
        singleRequest(req, "get_user_fills").mapTo[GetUserFills.Res]
      val res = Await.result(getFillsF, timeout.duration)
      if (assertFun(res)) {
        resOpt = Some(res)
      } else {
        Thread.sleep(200)
      }
    }
    if (resOpt.isEmpty) {
      throw new Exception(
        s"Timed out waiting for expectBalanceRes of req:${req} "
      )
    }
    resOpt
  }
}
