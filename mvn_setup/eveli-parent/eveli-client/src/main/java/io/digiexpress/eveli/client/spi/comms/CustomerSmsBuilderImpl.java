package io.digiexpress.eveli.client.spi.comms;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.CommsClient.CustomerSmsBuilder;

public class CustomerSmsBuilderImpl implements CustomerSmsBuilder {

  @Override
  public CustomerSmsBuilder title(String title) {
    return this;
  }

  @Override
  public CustomerSmsBuilder content(String content) {
    return this;
  }

  @Override
  public CustomerSmsBuilder userId(String userId) {
    return this;
  }

  @Override
  public CustomerSmsBuilder ssn(String userId) {
    return this;
  }

  @Override
  public CustomerSmsBuilder crn(String userId) {
    return this;
  }

  @Override
  public CustomerSmsBuilder notificationId(String notificationId) {
    return this;
  }

  @Override
  public void build() {
  }

}
