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

import io.digiexpress.eveli.client.api.CommsClient.CustomerMessageBuilder;

public class CustomerSmsBuilderImpl implements CustomerMessageBuilder {



  @Override
  public void build() {
  }

  @Override
  public CustomerMessageBuilder sms(String title, String content) {
    return this;
  }

  @Override
  public CustomerMessageBuilder email(String locale, String title, String content) {
    return this;
  }

  @Override
  public CustomerMessageBuilder senderId(String senderId) {
    return this;
  }

  @Override
  public CustomerMessageBuilder messageId(String messageId) {
    return this;
  }

}
