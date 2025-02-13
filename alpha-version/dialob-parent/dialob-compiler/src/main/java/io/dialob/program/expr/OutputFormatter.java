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
package io.dialob.program.expr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class OutputFormatter {
  private Locale locale;
  private DateTimeFormatter dateFormatter;
  private DateTimeFormatter timeFormatter;

  public OutputFormatter(String language) {
    locale = language != null ? new Locale(language) : Locale.getDefault();
    dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);
    timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale);
  }

  public String format(Object value) {
    return format(value, null);
  }

  public String format(Object value, String format) {
    if (value instanceof LocalDate) {
      DateTimeFormatter df = format == null ? dateFormatter : DateTimeFormatter.ofPattern(format, locale);
      return ((LocalDate) value).format(df);
    }
    if (value instanceof LocalTime) {
      DateTimeFormatter df = format == null ? timeFormatter : DateTimeFormatter.ofPattern(format, locale);
      return ((LocalTime) value).format(df);
    }
    if (value instanceof Number) {
      NumberFormat numberFormat = NumberFormat.getInstance(locale);
      if (isBlank(format)) {
        return numberFormat.format(value);
      } else {
        DecimalFormat df = ((DecimalFormat)numberFormat);
        df.applyPattern(format);
        return df.format(value);
      }
    }
    return value.toString();
  }

}
