package io.resys.wrench.assets.bundle.spi;

/*-
 * #%L
 * wrench-component-assets-integrations
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AssetIntegrationTest.ServiceTestConfig.class})
public class AssetIntegrationTest {
  @Autowired
  private ProgramEnvir envir;
  @Autowired
  private ApplicationContext context;

  @Configuration
  public static class ServiceTestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  @Test
  public void services() {
    final var services = envir.getValues();
    Assert.assertEquals(6, services.size());
  }

  @Test
  public void dt() throws IOException {
    final var dt = envir.getDecisionsByName().get("test decision table");
    Assert.assertEquals(ProgramStatus.UP, dt.getStatus());
  }

  @Test
  public void flow() throws IOException {
    final var flow = envir.getFlowsByName().get("evaluateRating");
    Assert.assertEquals(ProgramStatus.UP, flow.getStatus());
  }

  @Test
  public void flowTasks() {
    var task = envir.getServicesByName().get("RuleGroup1");
    Assert.assertEquals(ProgramStatus.UP, task.getStatus());

    task = envir.getServicesByName().get("RuleGroup2");
    Assert.assertEquals(ProgramStatus.UP, task.getStatus());
  }

  public String getContent(String location) throws IOException {
    return IOUtils.toString(context.getResource("classpath:" + location).getInputStream(), Charset.forName("utf-8"));
  }
}
