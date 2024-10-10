package io.resys.hdes.spring.env;

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

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;


@TestMethodOrder(MethodOrderer.MethodName.class)
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
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
      return new ObjectMapper()
              .registerModule(new Jdk8Module());
    }
  }

  @Test
  public void services() {
    final var services = envir.getValues();
    Assertions.assertEquals(6, services.size());
  }

  @Test
  public void dt() throws IOException {
    final var dt = envir.getDecisionsByName().get("test decision table");
    Assertions.assertEquals(ProgramStatus.UP, dt.getStatus());
  }

  @Test
  public void flow() throws IOException {
    final var flow = envir.getFlowsByName().get("evaluateRating");
    Assertions.assertEquals(ProgramStatus.UP, flow.getStatus());
  }

  @Test
  public void flowTasks() {
    var task = envir.getServicesByName().get("RuleGroup1");
    Assertions.assertEquals(ProgramStatus.UP, task.getStatus());

    task = envir.getServicesByName().get("RuleGroup2");
    Assertions.assertEquals(ProgramStatus.UP, task.getStatus());
  }

  public String getContent(String location) throws IOException {
    return IOUtils.toString(context.getResource("classpath:" + location).getInputStream(), Charset.forName("utf-8"));
  }
}
