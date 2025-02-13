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
package io.dialob.client.spi.executor.questionnaire;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ConversionUtil {

  public static Object toJSON(Object answer) {
    if (answer == null) {
      return null;
    }
    if (answer instanceof String[]) {
      return answer;
    }
    if (answer instanceof List) {
      return answer;
    }
    if (answer instanceof Date) {
      return ((Date) answer).toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    return answer.toString();
  }

  public static Object toJSONCompatible(Object answer) {
    if (answer == null) {
      return null;
    }
    if (answer instanceof String[]) {
      return answer;
    }
    if (answer instanceof List) {
      return answer;
    }
    if (answer instanceof Date) {
      return ((Date) answer).toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    if (answer instanceof LocalDate) {
      return answer.toString();
    }
    return answer;
  }

}
