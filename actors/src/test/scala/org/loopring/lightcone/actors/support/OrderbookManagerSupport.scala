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

package org.loopring.lightcone.actors.support

import org.loopring.lightcone.actors.core.OrderbookManagerActor
import org.loopring.lightcone.actors.validator._
import org.loopring.lightcone.proto.{XGetOrderbook, XMarketId}
import akka.pattern._
import scala.concurrent.Await

trait OrderbookManagerSupport {
  my: CommonSpec =>

  actors.add(
    OrderbookManagerActor.name,
    OrderbookManagerActor.startShardRegion
  )

  actors.add(
    OrderbookManagerMessageValidator.name,
    MessageValidationActor(
      new OrderbookManagerMessageValidator(),
      OrderbookManagerActor.name,
      OrderbookManagerMessageValidator.name
    )
  )

  //todo：因暂时未完成recover，因此需要发起一次请求，将shard初始化成功
  val orderBookInit = XGetOrderbook(
    0,
    100,
    Some(XMarketId(LRC_TOKEN.address, WETH_TOKEN.address))
  )
  val orderBookInitF = actors.get(OrderbookManagerActor.name) ? orderBookInit
  Await.result(orderBookInitF, timeout.duration)

}
