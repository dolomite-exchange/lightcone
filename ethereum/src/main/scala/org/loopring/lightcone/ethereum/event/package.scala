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

package org.loopring.lightcone.ethereum

import com.google.protobuf.ByteString
import org.loopring.lightcone.ethereum.abi._

package object event {
  val erc20Abi = ERC20ABI()
  val wethAbi = WETHABI()
  val tradeHistoryAbi = TradeHistoryAbi()
  val ringSubmitterAbi = RingSubmitterAbi()
  val loopringProtocolAbi = LoopringProtocolAbi()

  implicit def bytes2ByteString(bytes: Array[Byte]): ByteString =
    ByteString.copyFrom(bytes)

}
