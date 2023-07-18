package io.resys.hdes.spring.env;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/*-
 * #%L
 * wrench-assets-bundle
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {FlowTaskServiceExecutionTest.ServiceTestConfig.class})
public class FlowTaskServiceExecutionTest {

  @Autowired
  private ProgramEnvir envir;
  @Autowired
  private HdesClient client;
  @Autowired
  private ObjectMapper objectMapper;

  @Configuration
  public static class ServiceTestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  /**
   * Test for issue #1 topics 1 and 4.
   * @throws IOException
   */
  @Test
  public void flowExecution() throws IOException {
    
    final ObjectNode input = objectMapper.createObjectNode();
    input.put("val1", new BigDecimal("10"));
    input.put("val2", new BigDecimal("20"));
    
    final var body = client.executor(envir).inputJson(input).flow("sumFlow").andGetTask("SumTask");
    Assert.assertTrue(((BigDecimal) body.getReturns().get("sum")).compareTo(new BigDecimal("30")) == 0);
  }
}
