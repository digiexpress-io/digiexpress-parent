package io.thestencil.iam.spi.support;

/*-
 * #%L
 * stencil-iam-api
 * %%
 * Copyright (C) 2021 - 2024 Copyright 2021 ReSys OÜ
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

import io.thestencil.iam.api.RemoteIntegration;
import io.vertx.core.http.RequestOptions;

public class RemoteIntegrationConverter {

  public static RequestOptions integrationToOptions(RemoteIntegration integration) {
    boolean ssl = "https".equals(integration.getProtocol());
    Integer port = integration.getPort();
    if (port == null) {
      port = ssl ? 443 : 80;
    }
    else if (ssl && port == 80) {
      // ssl set but port default http, change it to default https
      port = 443;
    }
    return new RequestOptions()
        .setURI(integration.getPath())
        .setHost(integration.getHost())
        .setSsl(ssl)
        .setPort(port);
  }
}
