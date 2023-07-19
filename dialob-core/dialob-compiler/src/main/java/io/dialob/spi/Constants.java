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
package io.dialob.spi;

public final class Constants {

  public static final String QUESTIONNAIRE_CACHE_NAME = "questionnaireCache";
  public static final String FORM_CACHE_NAME = "formCache";
  public static final String PROGRAM_CACHE_NAME = "dialobProgramsCache";
  public static final String SESSION_CACHE_NAME = "sessionCache";
  public static final String SESSION_ACCESS_CACHE_NAME = "sessionAccessCache";

  public static final String QUESTIONNAIRE_CACHE_MANAGER_BEAN = QUESTIONNAIRE_CACHE_NAME + "Manager";
  public static final String FORM_CACHE_MANAGER_BEAN = FORM_CACHE_NAME + "Manager";
  public static final String PROGRAM_CACHE_MANAGER_BEAN = PROGRAM_CACHE_NAME + "Manager";
  public static final String SESSION_CACHE_MANAGER_BEAN = SESSION_CACHE_NAME + "Manager";
  public static final String SESSION_ACCESS_CACHE_MANAGER_BEAN = SESSION_ACCESS_CACHE_NAME + "Manager";


  public static final String QUESTIONNAIRE = "questionnaire";
  public static final String ERROR_CODE_REQUIRED = "REQUIRED";

  public static final String LATEST_REV = "LATEST";
  public static final String DEFAULT_TENANT = "00000000-0000-0000-0000-000000000000";

}
