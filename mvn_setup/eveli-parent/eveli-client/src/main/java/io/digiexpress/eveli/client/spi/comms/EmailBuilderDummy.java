package io.digiexpress.eveli.client.spi.comms;

import java.util.ArrayList;

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

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.CommsClient.EmailBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Email notification builder implementation based on Jakarta mail transport.
 */

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
@Slf4j
public class EmailBuilderDummy implements CommsClient.EmailBuilder {
  private final List<String> recipients = new ArrayList<>();
  private String title;
  private String message;
  private String refId;
  @Override
  public EmailBuilder recipientAddress(String recipientAddress) {
    recipients.add(recipientAddress);
    return this;
  }
  @Override
  public EmailBuilder recipientAddress(List<String> recipientAddress) {
    recipients.addAll(recipientAddress);
    return this;
  }
  
  @Override
  public void build() {
    log.debug("Email builder request for: title: {}, message: {}, refId: {}, recipients: {}",
        title, message, refId, recipients.stream().collect(Collectors.joining(", ")));
  }
}
