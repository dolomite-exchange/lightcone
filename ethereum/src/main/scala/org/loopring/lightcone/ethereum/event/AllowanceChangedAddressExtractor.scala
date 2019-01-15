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

package org.loopring.lightcone.ethereum.event

import com.google.inject.Inject
import com.typesafe.config.Config
import org.loopring.lightcone.ethereum.abi._
import org.loopring.lightcone.ethereum.data.Address
import org.loopring.lightcone.proto.{
  AddressAllowanceUpdated,
  Transaction,
  TransactionReceipt
}

import scala.collection.mutable.ListBuffer

class AllowanceChangedAddressExtractor @Inject()(implicit config: Config)
    extends EventExtractor[AddressAllowanceUpdated] {

  val protocolConf = config.getConfig("loopring_protocol")
  val delegateAddress = Address(protocolConf.getString("delegate-address"))
  val protocolAddress = Address(protocolConf.getString("protocol-address"))

  def extract(
      tx: Transaction,
      receipt: TransactionReceipt,
      blockTime: String
    ): Seq[AddressAllowanceUpdated] = {
    val allowanceAddresses = ListBuffer.empty[AddressAllowanceUpdated]
    receipt.logs.foreach { log =>
      wethAbi.unpackEvent(log.data, log.topics.toArray) match {
        case Some(transfer: TransferEvent.Result) =>
          if (Address(receipt.to).equals(protocolAddress))
            allowanceAddresses.append(
              AddressAllowanceUpdated(transfer.from, log.address)
            )

        case Some(approval: ApprovalEvent.Result) =>
          if (Address(approval.spender).equals(delegateAddress))
            allowanceAddresses.append(
              AddressAllowanceUpdated(approval.owner, log.address)
            )
        case _ =>
      }
    }
    if (isSucceed(receipt.status)) {
      wethAbi.unpackFunctionInput(tx.input) match {
        case Some(param: ApproveFunction.Parms) =>
          if (Address(param.spender).equals(delegateAddress))
            allowanceAddresses.append(
              AddressAllowanceUpdated(tx.from, tx.to)
            )
        case _ =>
      }
    }

    allowanceAddresses.distinct
  }
}