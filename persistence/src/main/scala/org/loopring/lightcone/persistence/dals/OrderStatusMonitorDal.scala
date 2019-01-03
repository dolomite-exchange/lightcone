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

package org.loopring.lightcone.persistence.dals

import org.loopring.lightcone.persistence.base.{enumColumnType, BaseDalImpl}
import org.loopring.lightcone.persistence.tables.OrderStatusMonitorTable
import org.loopring.lightcone.proto.OrderStatusMonitor
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}

trait OrderStatusMonitorDal
    extends BaseDalImpl[OrderStatusMonitorTable, OrderStatusMonitor] {

  def updateLatestProcessingTime(event: OrderStatusMonitor): Future[Int]

  def getLatestProcessingTime(
      monitoringType: String
    ): Future[Option[OrderStatusMonitor]]

}

class OrderStatusMonitorDalImpl(
  )(
    implicit val dbConfig: DatabaseConfig[JdbcProfile],
    val ec: ExecutionContext)
    extends OrderStatusMonitorDal {
  val query = TableQuery[OrderStatusMonitorTable]

  def updateLatestProcessingTime(event: OrderStatusMonitor): Future[Int] = {
    db.run(query.insertOrUpdate(event))
  }

  def getLatestProcessingTime(
      monitoringType: String
    ): Future[Option[OrderStatusMonitor]] =
    db.run(
      query
        .filter(_.monitoringType === monitoringType)
        .take(1)
        .result
        .headOption
    )

}