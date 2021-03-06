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

package io.lightcone.ethereum.abi

import org.ethereum.solidity.Abi
import org.web3j.utils.Numeric
import scala.annotation.meta.field
import scala.io.Source

class AuthorizableAbi(abiJson: String) extends AbiWrap(abiJson) {

  val isAddressAuthorizedFunction = IsAddressAuthorizedFunction(
    abi.findFunction(searchByName(IsAddressAuthorizedFunction.name))
  )

  val addressAuthorizedEvent = AddressAuthorizedEvent(
    abi.findEvent(searchByName(AddressAuthorizedEvent.name))
  )

  val addressDeauthorizedEvent = AddressDeauthorizedEvent(
    abi.findEvent(searchByName(AddressDeauthorizedEvent.name))
  )

  def unpackEvent(
      data: String,
      topics: Array[String]
    ): Option[Any] = {
    try {
      val event: Abi.Event = abi.findEvent(
        searchBySignature(
          Numeric.hexStringToByteArray(topics.headOption.getOrElse(""))
        )
      )
      event match {
        case _: Abi.Event =>
          event.name match {
            case AddressAuthorizedEvent.name =>
              addressAuthorizedEvent.unpack(data, topics)
            case AddressDeauthorizedEvent.name =>
              addressDeauthorizedEvent.unpack(data, topics)
            case _ => None
          }
        case _ => None
      }
    } catch {
      case _: Throwable => None
    }
  }

  override def unpackFunctionInput(data: String): Option[Any] = None
}

object AuthorizableAbi {

  val jsonStr: String =
    Source.fromResource("version2.0/Authorizable.abi").mkString
  def apply(abiJson: String): AuthorizableAbi = new AuthorizableAbi(abiJson)

  def apply(): AuthorizableAbi = new AuthorizableAbi(jsonStr)
}

class IsAddressAuthorizedFunction(val entry: Abi.Function)
    extends AbiFunction[
      IsAddressAuthorizedFunction.Params,
      IsAddressAuthorizedFunction.Result
    ]

object IsAddressAuthorizedFunction {
  val name = "isAddressAuthorized"

  case class Params(@(ContractAnnotation @field)("addr", 0) addr: String)

  case class Result(
      @(ContractAnnotation @field)("isAuthorized", 0) isAuthorized: Boolean)

  def apply(entry: Abi.Function): IsAddressAuthorizedFunction =
    new IsAddressAuthorizedFunction(entry)
}

class AddressAuthorizedEvent(val entry: Abi.Event)
    extends AbiEvent[AddressAuthorizedEvent.Result]

object AddressAuthorizedEvent {

  val name = "AddressAuthorized"

  case class Result(@(ContractAnnotation @field)("addr", 0) addr: String)

  def apply(entry: Abi.Event): AddressAuthorizedEvent =
    new AddressAuthorizedEvent(entry)
}

class AddressDeauthorizedEvent(val entry: Abi.Event)
    extends AbiEvent[AddressDeauthorizedEvent.Result]

object AddressDeauthorizedEvent {

  val name = "AddressDeauthorized"

  case class Result(@(ContractAnnotation @field)("addr", 0) addr: String)

  def apply(entry: Abi.Event): AddressDeauthorizedEvent =
    new AddressDeauthorizedEvent(entry)
}
