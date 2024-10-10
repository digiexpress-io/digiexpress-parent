package io.resys.wrench.assets.bundle.spi;

/*-
 * #%L
 * hdes-spring-composer
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableDebugRequest;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ServiceProgram;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.composer.DebugVisitor;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.MethodName.class)
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"wrench.assets.ide = false"})
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

    @Bean
    public HdesStore hdesStore(ObjectMapper objectMapper) {
      return HdesInMemoryStore.builder().objectMapper(objectMapper).build();
    }

    @Bean
    public ProgramEnvir staticAssets(HdesClient client) {
      final var source = client.store().query().get().await().atMost(Duration.ofMinutes(1));
      return ComposerEntityMapper.toEnvir(client.envir(), source).build();
    }
  }

  /**
   * Test for issue #1 topics 1 and 4.
   *
   * @throws IOException
   */
  @Test
  public void flowExecution() {

    final ObjectNode input = objectMapper.createObjectNode();
    input.put("val1", new BigDecimal("10"));
    input.put("val2", new BigDecimal("20"));

    final var body = client.executor(envir).inputJson(input).flow("sumFlow").andGetTask("SumTask");
    Assertions.assertEquals(0, ((BigDecimal) body.getReturns().get("sum")).compareTo(new BigDecimal("30")));
  }

  @Test
  public void passObjectToDT() throws JsonProcessingException {

    final JsonNode input = objectMapper.readTree("{\"fundAnswers\": {\"fundQuestionA\": \"selectionA\", \"fundQuestionB\": \"selectionA\"}}");

    final var result = client.executor(envir).inputJson(input).flow("objects").andGetBody().getReturns().get("result");
    Assertions.assertEquals("1", result);
  }

  @Test
  public void testHardcodedFlowInput() {
    final var body = client.executor(envir).inputField("myInputParam", "").flow("hardcodedInput").andGetBody();
    Assertions.assertEquals("test", body.getReturns().get("res"));
  }

  @Test
  public void testHardcodedFlowInputNull() {
    final var body = client.executor(envir).inputField("myInputParam", "").flow("hardcodedInputNull").andGetBody();
    Assertions.assertEquals(0, body.getReturns().size());
  }

  @Test
  public void flowCsvDebugSingle() {
    ImmutableDebugRequest entity = ImmutableDebugRequest.builder()
            .id("sumFlow.json")
            .inputCSV("val1;val2\n10;20")
            .build();

    var response = client.store().query().get().onItem()
            .transform(state -> new DebugVisitor(client).visit(entity, state)).await().atMost(Duration.ofMinutes(1));

    FlowProgram.FlowResult flowResult = (FlowProgram.FlowResult) response.getBody();

    Assertions.assertEquals(new BigDecimal("30"), flowResult.getReturns().get("sum"));
  }

  @Test
  public void flowCsvDebugMultiple() {
    ImmutableDebugRequest entity = ImmutableDebugRequest.builder()
            .id("sumFlow.json")
            .inputCSV("val1;val2\n10;20\n20;30")
            .build();

    var response = client.store().query().get().onItem()
            .transform(state -> new DebugVisitor(client).visit(entity, state)).await().atMost(Duration.ofMinutes(1));

    Assertions.assertLinesMatch(getFile("flowResult").lines(), response.getBodyCsv().lines());
  }

  @Test
  public void decisionCsvDebugSingle() {
    ImmutableDebugRequest entity = ImmutableDebugRequest.builder()
            .id("decimalTest.json")
            .inputCSV("letterCode\nM")
            .build();

    var response = client.store().query().get().onItem()
            .transform(state -> new DebugVisitor(client).visit(entity, state)).await().atMost(Duration.ofMinutes(1));

    DecisionProgram.DecisionResult decisionResult = (DecisionProgram.DecisionResult) response.getBody();

    Assertions.assertEquals(new BigDecimal("3.4"), decisionResult.getMatches().get(0).getReturns().get(0).getUsedValue());
  }

  @Test
  public void decisionCsvDebugMultiple() {
    ImmutableDebugRequest entity = ImmutableDebugRequest.builder()
            .id("decimalTest.json")
            .inputCSV("letterCode\nS\nM")
            .build();

    var response = client.store().query().get().onItem()
            .transform(state -> new DebugVisitor(client).visit(entity, state)).await().atMost(Duration.ofMinutes(1));

    Assertions.assertLinesMatch(getFile("decisionResult").lines(), response.getBodyCsv().lines());
  }

  @Test
  public void serviceCsvDebugSingle() throws JsonProcessingException {
    ImmutableDebugRequest entity = ImmutableDebugRequest.builder()
            .id("SumTask.json")
            .inputCSV("val1;val2\n10;20")
            .build();

    var response = client.store().query().get().onItem()
            .transform(state -> new DebugVisitor(client).visit(entity, state)).await().atMost(Duration.ofMinutes(1));

    ServiceProgram.ServiceResult serviceResult = (ServiceProgram.ServiceResult) response.getBody();

    Assertions.assertEquals("{\"sum\":30}", objectMapper.writeValueAsString(serviceResult.getValue()));
  }

  @Test
  public void serviceCsvDebugMultiple() {
    ImmutableDebugRequest entity = ImmutableDebugRequest.builder()
            .id("SumTask.json")
            .inputCSV("val1;val2\n10;20\n20;30")
            .build();

    var response = client.store().query().get().onItem()
            .transform(state -> new DebugVisitor(client).visit(entity, state)).await().atMost(Duration.ofMinutes(1));

    Assertions.assertLinesMatch(getFile("serviceResult").lines(), response.getBodyCsv().lines());
  }

  public String getFile(String name) {
    try {
      return IOUtils.toString(FlowTaskServiceExecutionTest.class.getClassLoader().getResourceAsStream("assets/debug/" + name + ".csv"), StandardCharsets.UTF_8);
    } catch (IOException e) {
      return e.getMessage();
    }
  }
}
