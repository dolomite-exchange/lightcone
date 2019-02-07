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

import io.lightcone.persistence.base._
import io.lightcone.relayer.data._
import io.lightcone.core._
import slick.jdbc.MySQLProfile.api._

class OrderStatusMonitorTable(tag: Tag)
    extends BaseTable[OrderStatusMonitor](tag, "T_ORDER_STATUS_MONITOR") {

  def id = monitoringType
  def processTime = column[Long]("process_time")
  def monitoringType = column[String]("monitoring_type", O.PrimaryKey, O.Unique)

  def * =
    (monitoringType, processTime) <> ((OrderStatusMonitor.apply _).tupled, OrderStatusMonitor.unapply)

}
