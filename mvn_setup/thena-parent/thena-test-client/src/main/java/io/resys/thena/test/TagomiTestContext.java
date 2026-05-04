package io.resys.thena.test;

/*-
 * #%L
 * thena-test-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.resys.thena.test.TagomiTest.TagomiUrl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TagomiTestContext {
  private GenericContainer<?> tagomi;
  private String tagomiBaseUrl;

  @SuppressWarnings("resource")
  public void initialize(TagomiTest data) {
    if (!data.enabled()) {
      return;
    }

    tagomi = new GenericContainer<>("docker.resys.io/digiexpress-io/tagomi-pdf:0.1.4")
        .withCreateContainerCmdModifier(cmd -> cmd.withPlatform("linux/amd64"))
        .withExposedPorts(8085)
        .withEnv("HTTPS_ENABLED", "false")
        .withEnv("PORT", "8085")
        .withEnv("FONTS_PATH", "/app/assets/fonts")
        .withEnv("USE_SYSTEM_FONTS", "true")
        .withEnv("PACKAGES_PATH", "/app/assets/packages")
        .withEnv("RUST_LOG", "info")
        .waitingFor(Wait.forHttp("/health").forPort(8085).forStatusCode(200));

    tagomi.start();
    tagomiBaseUrl = "http://" + tagomi.getHost() + ":" + tagomi.getMappedPort(8085);

    log.info("Tagomi PDF service is running at: {}", tagomiBaseUrl);
  }

  public void cleanup() {
    if (tagomi != null) {
      tagomi.stop();
    }
  }

  public TagomiUrl getTagomiUrl() {
    return new TagomiUrl(tagomiBaseUrl);
  }
}
