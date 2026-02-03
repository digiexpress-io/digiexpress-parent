package io.digiexpress.eveli.client.test.feedback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.config.EveliAutoConfigJpa;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.digiexpress.eveli.client.spi.feedback.FeedbackCategoriesReaderImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackClientImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory;
import io.digiexpress.eveli.client.test.BaseEnvir;
import io.digiexpress.eveli.dialob.spi.DialobClientImpl;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = { EveliAutoConfigJpa.class, FeedbackEnvirSetup.FeedbackEnvirSetupConfig.class })
public abstract class FeedbackEnvirSetup {

  private static PostgreSQLContainer<?> CONTAINER;
  private static AtomicInteger TEST_INDEX = new AtomicInteger(0);
  
  
  public static void start(PostgreSQLContainer<?> container) {
    CONTAINER = container;
    CONTAINER.start();
    for (int i = 0; i < 100; i++) {
      try (var c = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
        break;
      } catch (SQLException e) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      }
    }

  }  
  public static void end() {
    CONTAINER.stop();
  }  
  
  
  @Configuration
  public static class FeedbackEnvirSetupConfig {
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @Autowired ObjectMapper objectMapper;
    @Autowired TransactionTemplate tx;
    @Autowired ApplicationEventPublisher publisher;

    @Bean
    public TaskClient taskClient(ApplicationEventPublisher publisher) {
      final var repoId = "test-task-client-" + TEST_INDEX.incrementAndGet();
      final var setup = new FeedbackTaskEnvirSetup(CONTAINER, repoId);
      return setup.getTaskClient();
    }
    
    @Bean
    public SetupTask setupTask(TaskClient taskClient) {    
      return new SetupTask(taskClient);
    }


    @Bean FeedbackCategoriesReader feedbackCategoriesReader(ObjectMapper objectMapper) {
      return new FeedbackCategoriesReaderImpl(objectMapper);
    }
    
    @Bean
    public FeedbackClient feedbackClient(TaskClient taskClient, FeedbackCategoriesReader feedbackCategoriesReader) {
      final var feedbackWithHistory = new FeedbackWithHistory(tx, jdbcTemplate, objectMapper);
      final var dialobClient = new DialobClientImpl(objectMapper, null);
      final var configProps = new EveliPropsFeedback();

      configProps.setForms("palautteet");
      configProps.setCategoryMain("mainList");
      configProps.setCategorySub("cityServiceGroup, preschoolEducationGroup, cityServiceMainList, constructionMainList, youthServiceMainList, exerciseMainList, schoolMainList, employmentImmigrationMainList, freeTimeCultureMainList, preschoolMainList, communicationMainList, cooperationMainList");

      configProps.setQuestion("feedBackTxt");
      configProps.setQuestionTitle("feedBackTitle");
      configProps.setUsername("FirstNames, LastName");
      configProps.setUsernameAllowed("publicAnswerAllowed");
      
      return new FeedbackClientImpl(taskClient, dialobClient, jdbcTemplate, feedbackWithHistory, configProps, objectMapper, feedbackCategoriesReader);

    }
  }
  
  
  @RequiredArgsConstructor
  public static class SetupTask {
    private final TaskClient taskClient;
    
    @Transactional
    public String generateOneTask() {
      final var user = BaseEnvir.FAKER.starTrek().character();
      final var email = user+"@resys.io";
      final var task = taskClient.taskBuilder()
        .userId(user, email)
        .createTask(ImmutableCreateTaskCommand.builder()
        .subject(BaseEnvir.FAKER.book().title())
        // debugging delay
        .build()).await().atMost(Duration.ofMinutes(5));
      
      taskClient.taskBuilder()
        .userId(user, email)
        .createTaskComment(ImmutableCreateTaskCommentCommand.builder()
            .external(true)
            .commentText(BaseEnvir.FAKER.chuckNorris().fact())
            .taskId(task.getId())
            .source(TaskCommentSource.FRONTDESK)
            .build()).await().atMost(Duration.ofMinutes(1));
    
      
      final var formBody = new JsonObject(fileToString("feedback/filled-form.json"));
      
      final var process = taskClient.createProcess()
        .parentArticleName(null)  
        .articleName("no-article")
        .flowName("no-flow-name")
        .formName("no-form-name")
        .workflowName("no-workflow")
        .questionnaireId(formBody.getString("_id") + task.getId())
        .userId(BaseEnvir.FAKER.idNumber().ssnValid())
      
        
        .formTagName("dev")
        .stencilTagName("dev")
        .wrenchTagName("dev")
        .commitAuthor("x")
        .commitMessage("x")
        .build()
        
        .await().atMost(Duration.ofMinutes(1));
      
      taskClient.modifyProcess()
        .commitAuthor("x")
        .commitMessage("x")
        .id(process.getId().toString())
        .merge((current, merger) -> merger
            .status(GrimProcessStatus.ANSWERED)
            .taskId(task.getId())
            .formBody(formBody.toString()).build())
        .build()
        .await().atMost(Duration.ofMinutes(1));
      
      return task.getId().toString();
    }
  }
  
  public static String fileToString(String resource) {
    try {
      return IOUtils.toString(FeedbackEnvirSetup.class.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


}
