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

package io.lightcone.relayer.validator

import com.typesafe.config.Config
import io.lightcone.core._
import io.lightcone.lib._
import io.lightcone.relayer.actors.ActivityActor
import io.lightcone.relayer.data._
import scala.concurrent.{ExecutionContext, Future}

object ActivityValidator {
  val name = "activity_validator"
}

final class ActivityValidator(
  )(
    implicit
    val config: Config,
    ec: ExecutionContext)
    extends MessageValidator {

  val activityConfig = config.getConfig(ActivityActor.name)

  val defaultItemsPerPage = activityConfig.getInt("default-items-per-page")
  val maxItemsPerPage = activityConfig.getInt("max-items-per-page")
  implicit val pageConfig = PageConfig(defaultItemsPerPage, maxItemsPerPage)

  def validate = {

    case req: GetActivities.Req =>
      Future {
        val owner =
          if (req.owner.isEmpty)
            throw ErrorException(
              ErrorCode.ERR_INVALID_ARGUMENT,
              "Parameter owner could not be empty"
            )
          else Address.normalize(req.owner)
        GetActivities.Req(
          owner,
          req.token,
          req.sort,
          MessageValidator.getValidCursorPaging(req.paging)
        )
      }

  }
}
