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

import org.web3j.utils.Numeric
import com.google.protobuf.ByteString

class Bitstream(private var data: String = "") {
  private val ADDRESS_LENGTH = 20
  private val Uint256Max = BigInt("f" * 64, 16)

  def getData = if (data.length == 0) "0x0" else "0x" + data

  def getBytes = Numeric.hexStringToByteArray(data)
  def length = data.length / 2

  def addAddress(
      x: String,
      forceAppend: Boolean = false
    ): Int =
    addAddress(x, ADDRESS_LENGTH, forceAppend)

  def addAddress(
      x: String,
      numBytes: Int,
      forceAppend: Boolean
    ) = {
    val _x = if (x.length == 0) "0" else x

    insert(
      Numeric.toHexStringNoPrefixZeroPadded(
        Numeric.toBigInt(_x),
        numBytes * 2
      ),
      forceAppend
    )
  }

  def addUint16(
      num: BigInt,
      forceAppend: Boolean = true
    ) =
    addBigInt(num, 2, forceAppend)

  def addInt16(
      num: BigInt,
      forceAppend: Boolean = true
    ) =
    if (num >= 0) {
      addBigInt(num, 2, forceAppend)
    } else {
      val negUint256 = Uint256Max + num + 1
      val int16Str = negUint256.toString(16).substring(60, 64)
      addHex(int16Str, forceAppend)
    }

  def addUint32(
      num: BigInt,
      forceAppend: Boolean = true
    ) =
    addBigInt(num, 4, forceAppend)

  def addUint(
      num: BigInt,
      forceAppend: Boolean = true
    ) =
    addBigInt(num, 32, forceAppend)

  def addNumber(
      num: BigInt,
      numBytes: Int,
      forceAppend: Boolean = true
    ) =
    addBigInt(num, numBytes, forceAppend)

  def addBoolean(
      b: Boolean,
      forceAppend: Boolean = true
    ) =
    addBigInt(if (b) 1 else 0, 1, forceAppend)

  def addBytes32(
      str: String,
      forceAppend: Boolean = true
    ) {
    val strWithoutPrefix = Numeric.cleanHexPrefix(str)
    if (strWithoutPrefix.length > 64) {
      throw new IllegalArgumentException(
        s"invalid bytes32 str: too long, str:$str"
      )
    }
    val strPadded = strWithoutPrefix + "0" * (64 - strWithoutPrefix.length)
    insert(strPadded, forceAppend)
  }

  def addHex(
      str: String,
      forceAppend: Boolean = true
    ) =
    insert(Numeric.cleanHexPrefix(str), forceAppend)

  def addRawBytes(
      bytes: Array[Byte],
      forceAppend: Boolean = true
    ) =
    insert(Numeric.cleanHexPrefix(Numeric.toHexString(bytes)), forceAppend)

  private def addBigInt(
      num: BigInt,
      numBytes: Int,
      forceAppend: Boolean = true
    ) = insert(
    Numeric.toHexStringNoPrefixZeroPadded(
      num.bigInteger,
      numBytes * 2
    ),
    forceAppend
  )

  private def insert(
      x: String,
      forceAppend: Boolean
    ): Int = {
    var offset = length

    if (!forceAppend) {
      // Check if the data we're inserting is already available in the bitstream.
      // If so, return the offset to the location.
      var start = 0
      while (start != -1) {
        start = data.indexOf(x, start)
        if (start != -1) {
          if ((start % 2) == 0) {
            offset = start / 2
            return offset
          } else {
            start += 1
          }
        }
      }
    }

    data ++= x
    offset
  }

  private def hex2Int(hex: String): Int = Integer.parseInt(hex, 16)

  def extractUint8(offset: Int): Int = hex2Int(extractBytesX(offset, 1))

  def extractUint16(offset: Int): Int = hex2Int(extractBytesX(offset, 2))

  def extractInt16(offset: Int): Int = {
    val hex = extractBytesX(offset, 2)
    val uint16 = BigInt(hex, 16)
    val resBigInt = if ((uint16 >> 15) == 1) {
      uint16 - BigInt("ffff", 16) - 1
    } else {
      uint16
    }

    resBigInt.toInt
  }

  def extractUint32(offset: Int): Int = hex2Int(extractBytesX(offset, 4))

  def extractUint(offset: Int): ByteString = {
    val hexStr = extractBytesX(offset, 32)
    ByteString.copyFromUtf8(hexStr)
  }

  def extractAddress(offset: Int) = "0x" + extractBytesX(offset, 20)

  def extractBytesX(
      offset: Int,
      numBytes: Int
    ) = {
    val start = offset * 2
    val end = start + numBytes * 2

    require(this.data.length > end, "substring index out of range.")
    this.data.substring(start, end)
  }

}
