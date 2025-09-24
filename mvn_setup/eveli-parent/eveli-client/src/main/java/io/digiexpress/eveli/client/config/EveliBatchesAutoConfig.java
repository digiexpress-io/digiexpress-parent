package io.digiexpress.eveli.client.config;

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

import java.time.Duration;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.config.EveliBatchesAutoConfig.BatchTenantCondition;
import io.digiexpress.eveli.client.spi.batch.reject_stale_forms.BatchJob_RejectStaleForms_Definition;
import io.digiexpress.eveli.client.spi.tenant.TenantConfigClientProps;
import io.digiexpress.eveli.client.web.resources.worker.BatchApiCotroller;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.BatchClient.BatchDefinition;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.spi.BatchClientImpl;
import io.digiexpress.thena.batch.client.spi.persistence.sql.BatchDbImpl;
import lombok.extern.slf4j.Slf4j;



@Configuration
@org.springframework.context.annotation.Conditional(BatchTenantCondition.class)
@Slf4j
public class EveliBatchesAutoConfig {
  
  static class BatchTenantCondition extends EveliTenantCondition {
    public BatchTenantCondition() {
      super(TenantConfigClientProps.BATCHES, TenantConfigClientProps.BATCHES_DEV);
    }
  }

  @Bean
  public BatchDefinition tasksCleanUpStaleDataJob(TaskClient taskClient, DialobClient dialobClient) {
    return BatchJob_RejectStaleForms_Definition.create(taskClient, dialobClient);
  }
  
  @Bean
  public BatchClient batchClient(io.vertx.mutiny.pgclient.PgPool pgPool) {
    return BatchDbImpl.create().tenant("batch").client(pgPool).build()
      .createIfNot()
      .onItem().transform(store -> new BatchClientImpl(store))
      .await().atMost(Duration.ofMinutes(1));
  }
  
  @Bean
  public BatchConfig batchConfig(BatchClient client, List<BatchDefinition> definitions) {
    return client
        .createBatchConfig()
        .appId("eveli-app")
        .commitAuthor("spring-boot-bean")
        .commitMessage("default system config, with no. of jobs: " + definitions.size())
        .addAll(definitions)
        .build()
      .onItem().transform(e -> e.getObject())
      .await().atMost(Duration.ofMinutes(1));
  }
  
  @Bean
  public BatchApiCotroller batchApiCotroller(BatchClient client, BatchConfig config, EveliPropsBatch props, WorkerAuthClient auth, ApplicationEventPublisher publisher) {
    return new BatchApiCotroller(auth, client, config, props, publisher);
  }
}
