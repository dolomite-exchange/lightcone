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

trait PendingRingPool {
  // This reset method should be used only by tests.
  def reset(): Unit

  def getOrderPendingAmountS(orderId: String): BigInt

  def hasRing(ringId: String): Boolean
  def addRing(ring: MatchableRing): Boolean
  def deleteRing(ringId: String): Set[String]

  def deleteRingsBefore(timestamp: Long): Set[String]
  def deleteRingsOlderThan(ageInSeconds: Long): Set[String]
}
