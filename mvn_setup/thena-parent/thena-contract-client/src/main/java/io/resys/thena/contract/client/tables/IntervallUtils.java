package io.resys.thena.contract.client.tables;

/*-
 * #%L
 * thena-contract-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.Period;
import java.util.Optional;
import java.util.UUID;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.pgclient.data.Interval;
import io.vertx.sqlclient.data.NullValue;

public class IntervallUtils {

  /**
   * Converts a Duration to PostgreSQL Interval for database storage.
   * 
   * @param duration the Duration to convert
   * @return Interval object
   * @throws IllegalArgumentException if the duration is null
   */
  public static Interval toInterval(Period period) {
    if (period == null) {
      throw new IllegalArgumentException("Period cannot be null");
    }

    return new Interval()
      .years(period.getYears())
      .months(period.getMonths())
      .days(period.getDays());
  }
  
  /**
   * Converts an Optional Duration to PostgreSQL Interval or NullValue.
   * 
   * @param optionalDuration the Optional Duration to convert
   * @return Interval object or NullValue.of(Interval.class) if empty
   * @throws IllegalArgumentException if the duration is present but cannot be converted
   */
  public static Object toIntervalOptional(Optional<Period> optionalDuration) {
    if(optionalDuration.isEmpty()) {
      return NullValue.of(Interval.class);
    }
    return optionalDuration.map(IntervallUtils::toInterval);
  }
  
  /**
   * Safely converts a Duration to PostgreSQL Interval, returning NullValue for null input.
   * 
   * @param duration the Duration to convert (can be null)
   * @return Interval object or NullValue.of(Interval.class) if input is null
   * @throws IllegalArgumentException if the duration is not null but cannot be converted
   */
  public static Object toIntervalSafe(Period duration) {
    return duration == null ? NullValue.of(Interval.class) : toInterval(duration);
  }
  
  public static Period toDuration(Row row, String name) {
    final var interval = row.get(Interval.class, name);
    if (interval == null) {
        return null;
    }
    
    return Period.of(
        interval.getYears(),
        interval.getMonths(),
        interval.getDays()
    );
  }
  
  public static String toStringUUID(Row row, String name) {
    final var uuid = row.get(UUID.class, name);
    return uuid == null ? null : uuid.toString();
  }
}
