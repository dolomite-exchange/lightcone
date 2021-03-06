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

package io.lightcone.persistence.dals

import io.lightcone.persistence._
import io.lightcone.persistence.base._
import slick.jdbc.MySQLProfile.api._

//TODO(yongfeng)：初始化配置时需要把eth，btc配进去
class CMCCrawlerConfigForTokenTable(tag: Tag)
    extends BaseTable[CMCCrawlerConfigForToken](
      tag,
      "T_CMC_CRAWLER_CONFIG_FOR_TOKEN"
    ) {

  def id = slug
  def slug = column[String]("slug", O.SqlType("VARCHAR(20)"), O.PrimaryKey)
  def symbol = column[String]("symbol", O.SqlType("VARCHAR(10)"))

  // indexes
  def idx_symbol = index("idx_symbol", (symbol), unique = true)

  def * =
    (
      symbol,
      slug
    ) <> ((CMCCrawlerConfigForToken.apply _).tupled, CMCCrawlerConfigForToken.unapply)
}
