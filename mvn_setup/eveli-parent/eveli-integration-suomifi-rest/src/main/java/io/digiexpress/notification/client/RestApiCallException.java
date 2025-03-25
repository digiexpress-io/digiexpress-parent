package io.digiexpress.notification.client;

/*-
 * #%L
 * eveli-integration-suomifi-rest
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

import org.springframework.http.HttpStatusCode;

public class RestApiCallException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final RestErrorResponse response;
  private final HttpStatusCode code;
  
  public RestApiCallException(RestErrorResponse response, HttpStatusCode httpStatusCode) {
    super(response.getReason());
    this.response = response;
    this.code = httpStatusCode;
  }

  public RestErrorResponse getResponse() {
    return response;
  }

  public HttpStatusCode getCode() {
    return code;
  }

}
