package io.digiexpress.eveli.client.test.gamut;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.persistence.ImmutableAuthoringConfig;
import io.resys.limaone.persistence.world.WorldBuilderImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDbImpl;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.ModelDbConfig;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.fs.spi.FileSystem_ThenaImpl;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.resys.thena.jackson.QuarkusJacksonJsonCodec;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.test.DialobTest.FormUrl;
import io.resys.thena.test.ThenaTest;
import io.resys.thena.test.ThenaTestDbConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Pool;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@ThenaTest(database = @ThenaTestDbConfig(enabled = false))
public class GamutTestConfig {
  protected static final Duration AT_MOST = Duration.ofMinutes(1);
  protected TaskClientImpl taskClient;
  private FileSystem_ThenaImpl fsClient;

  @BeforeEach
  public void setUp(Pool pgPool) {
    // thena filesystem for limaone model storage
    this.fsClient = FileSystem_ThenaImpl.createInstance()
      .tenantName("gamut-fs")
      .client(pgPool)
      .errorHandler(new PgErrors())
      .build();

    this.fsClient.tenants()
      .createOneTenant()
      .name("gamut-fs", StructureType.fs)
      .buildOnlyIfNotCreated()
      .await().atMost(AT_MOST);

    this.fsClient.withTenant()
      .commitBuilder()
      .branchName("main")
      .commitAuthor("test")
      .commitMessage("init")
      .newFile(f -> f.fileName("init.md").fileType("init_md").fileValue(new JsonObject()).build())
      .build()
      .await().atMost(AT_MOST);

    // grim tenant for TaskClient (gamut task/process storage)
    final var grimClient = GrimClientImpl.create()
      .db("gamut-grim")
      .client(pgPool)
      .build();

    grimClient.tenants().createOneTenant()
      .name("gamut-grim", StructureType.grim)
      .buildOnlyIfNotCreated()
      .await().atMost(AT_MOST);

    final var taskStore = new TaskStoreImpl(ImmutableTaskStoreConfig.builder()
      .tenantName("gamut-grim")
      .client(grimClient)
      .build());
    this.taskClient = new TaskClientImpl(null, null, taskStore, null);
  }

  public AuthoringImpl.AuthoringConfig createAuthoringConfig(FormDb formDb) {
    final var envir = DefaultEnvironmentProperties.builder()
      .formDb(formDb)
      // used to make TaskClient available to the flow tasks at runtime
      .di(new DefaultEnvironmentProperties.DI() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> T getBean(Class<T> type) {
          if (TaskClient.class.isAssignableFrom(type)) {
            return (T) taskClient;
          }
          throw new UnsupportedOperationException("No bean of type: " + type);
        }
      })
      .dbConfig(ModelDbConfig.filesystem(fsClient))
      .defaultTenantName(fsClient.getTenantName())
      .build();

    return ImmutableAuthoringConfig.builder()
      .envir(envir)
      .persistence(envir.getModelDb().withBranchName(Optional.of(WorldBuilderImpl.branchName)))
      .build();
  }

  public static FormDbImpl createFormDb(FormUrl formUrl) {
    final var formHttp = new RestTemplate();
    formHttp.setUriTemplateHandler(new DefaultUriBuilderFactory(formUrl.getFormUrl() + "/dialob/api"));

    final var questionnaireHttp = new RestTemplate();
    questionnaireHttp.setUriTemplateHandler(new DefaultUriBuilderFactory(formUrl.getSessionUrl() + "/session/dialob"));

    return FormDbImpl.builder()
      .questionnaireHttp(questionnaireHttp)
      .formHttp(formHttp)
      .objectMapper(QuarkusJacksonJsonCodec.mapper())
      .build();
  }

  public static String loadResource(String path) {
    try {
      return IOUtils.toString(
        GamutTaskCreationTest.class.getClassLoader().getResource(path),
        StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource: " + path, e);
    }
  }

}
