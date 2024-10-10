/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
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
package io.dialob.rule.parser;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

public class PeriodUtil {

  public static int comparePeriods(Period lhs, Period rhs) {
    if (lhs.getYears() != rhs.getYears()) {
      return Integer.compare(lhs.getYears(), rhs.getYears());
    }
    if (lhs.getMonths() != rhs.getMonths()) {
      return Integer.compare(lhs.getMonths(), rhs.getMonths());
    }
    if (lhs.getDays() != rhs.getDays()) {
      return Integer.compare(lhs.getDays(), rhs.getDays());
    }
    return 0;
  }

  public static LocalDate datePlusPeriod(LocalDate lhs, Period rhs) {
    return lhs.plus(rhs);
  }

  public static LocalDate periodPlusDate(Period lhs, LocalDate rhs) {
    return rhs.plus(lhs);
  }


  public static LocalDate dateMinusPeriod(LocalDate lhs, Period rhs) {
    return lhs.minus(rhs);
  }


  public static Period sumPeriods(Period lhs, Period rhs) {
    return lhs.plus(rhs).normalized();
  }

  public static Duration sumDurations(Duration lhs, Duration rhs) {
    return lhs.plus(rhs);
  }

  public static Period minusPeriods(Period lhs, Period rhs) {
    return lhs.minus(rhs).normalized();
  }

  public static Duration minusDurations(Duration lhs, Duration rhs) {
    return lhs.minus(rhs);
  }


  public static LocalTime timePlusDuration(LocalTime lhs, Duration rhs) {
    return lhs.plus(rhs);
  }

  public static LocalTime durationPlusTime(Duration lhs, LocalTime rhs) {
    return rhs.plus(lhs);
  }

  public static LocalTime timeMinusDuration(LocalTime lhs, Duration rhs) {
    return lhs.minus(rhs);
  }


}
