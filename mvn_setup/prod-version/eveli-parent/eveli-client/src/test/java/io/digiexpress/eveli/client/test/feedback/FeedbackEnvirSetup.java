package io.digiexpress.eveli.client.test.feedback;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.config.EveliAutoConfigDB;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.digiexpress.eveli.client.event.TaskEventPublisher;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.feedback.FeedbackClientImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.test.BaseEnvir;
import io.digiexpress.eveli.client.test.task.TaskEnvirSetup;
import io.digiexpress.eveli.dialob.spi.DialobClientImpl;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Testcontainers
@EnableAutoConfiguration
@ContextConfiguration(classes = { EveliAutoConfigDB.class, FeedbackEnvirSetup.FeedbackEnvirSetupConfig.class })
public abstract class FeedbackEnvirSetup {

  private static PostgreSQLContainer<?> CONTAINER;
  private static AtomicInteger TEST_INDEX = new AtomicInteger(0);
  
  
  public static void start(PostgreSQLContainer<?> container) {
    CONTAINER = container;
    CONTAINER.start();
  }  
  public static void end() {
    CONTAINER.stop();
  }  
  
  
  @Configuration
  public static class FeedbackEnvirSetupConfig {
    @MockBean TaskNotificator notificator;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @Autowired ProcessRepository processJPA;
    @Autowired ObjectMapper objectMapper;
    @Autowired TransactionTemplate tx;
    @Autowired ApplicationEventPublisher publisher;

    @Bean
    public TaskClient taskClient(ApplicationEventPublisher publisher) {
      final var repoId = "test-task-client-" + TEST_INDEX.incrementAndGet();
      final var setup = new TaskEnvirSetup(CONTAINER, new TaskEventPublisher(publisher), repoId);
      return setup.getTaskClient();
    }
    
    @Bean
    public SetupTask setupTask(ProcessClient processClient, TaskClient taskClient) {    
      return new SetupTask(taskClient, processClient);
    }

    @Bean
    public ProcessClient processClient() {
      final var processClient = new ProcessClientImpl(processJPA, null, null, null, null);
      return processClient;
    }
    
    @Bean
    public FeedbackClient feedbackClient(ProcessClient processClient, TaskClient taskClient) {
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
      
      return new FeedbackClientImpl(taskClient, processClient, dialobClient, jdbcTemplate, feedbackWithHistory, configProps);

    }
  }
  
  
  @RequiredArgsConstructor
  public static class SetupTask {
    private final TaskClient taskClient;
    private final ProcessClient processClient;
    
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
      
      final var comment = taskClient.taskBuilder()
        .userId(user, email)
        .createTaskComment(ImmutableCreateTaskCommentCommand.builder()
            .external(true)
            .commentText(BaseEnvir.FAKER.chuckNorris().fact())
            .taskId(task.getId())
            .source(TaskCommentSource.FRONTDESK)
            .build());
    
      
      final var formBody = new JsonObject(fileToString("feedback/filled-form.json"));
      
      final var process = processClient.createInstance()
        .parentArticleName(null)  
        .articleName("no-article")
        .flowName("no-flow-name")
        .formName("no-form-name")
        .workflowName("no-workflow")
        .questionnaireId(formBody.getString("_id") + task.getId())
        .userId(BaseEnvir.FAKER.idNumber().ssnValid())
      
        
        .formTagName("dev")
        .stencilTagName("dev")
        .workflowTagName("dev")
        .wrenchTagName("dev")
        .create();
      
      processClient.changeInstanceStatus().answeredByQuestionnaire(process.getQuestionnaireId(), task.getId().toString());
      processClient.createBodyBuilder().processInstanceId(process.getId()).formBody(formBody.toString()).build();
      
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
