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

import com.google.inject.Inject
import com.google.inject.name.Named
import io.lightcone.core.ErrorCode._
import io.lightcone.persistence._
import slick.basic._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent._
import scala.util.{Failure, Success}

class CMCTickersInUsdDalImpl @Inject()(
    implicit
    val ec: ExecutionContext,
    @Named("dbconfig-cmc-tickers-in-usd") val dbConfig: DatabaseConfig[
      JdbcProfile
    ])
    extends CMCTickersInUsdDal {

  val query = TableQuery[CMCTickersInUsdTable]

  def saveTickers(tickers: Seq[CMCTickersInUsd]) =
    db.run((query ++= tickers).asTry).map {
      case Failure(ex) => {
        logger.error(s"save tickers error : ${ex.getMessage}")
        ERR_PERSISTENCE_INTERNAL
      }
      case Success(x) =>
        ERR_NONE
    }

  def getTickersByJob(jobId: Int): Future[Seq[CMCTickersInUsd]] =
    db.run(query.filter(_.batchId === jobId).result)

  def countTickersByJob(jobId: Int) =
    db.run(query.filter(_.batchId === jobId).size.result)

  def getTickers(
      jobId: Int,
      tokenSlugs: Seq[String]
    ): Future[Seq[CMCTickersInUsd]] =
    db.run(
      query
        .filter(_.batchId === jobId)
        .filter(_.slug inSet tokenSlugs)
        .result
    )
}