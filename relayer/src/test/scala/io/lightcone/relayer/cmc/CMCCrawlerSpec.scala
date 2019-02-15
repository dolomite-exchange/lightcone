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

package io.lightcone.relayer.cmc

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.pattern._
import akka.testkit.TestProbe
import io.lightcone.core._
import io.lightcone.relayer.actors._
import io.lightcone.relayer.data._
import io.lightcone.relayer.support._
import io.lightcone.relayer.validator._
import scala.concurrent.Await
import scala.concurrent.duration._

class CMCCrawlerSpec
    extends CommonSpec
    with HttpSupport
    with EthereumSupport
    with DatabaseModuleSupport
    with MetadataManagerSupport
    with CMCSupport {

  val probe = TestProbe()

  def actor = actors.get(CMCCrawlerActor.name)

  "aaa" must {
    "bbb" in {
      Thread.sleep(30000)
    }
  }

}