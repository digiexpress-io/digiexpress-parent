package io.digiexpress.eveli.client.test.task;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.ImmutableModifyTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.test.BaseEnvir;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstSource;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.ImmutableProgramEnvir;
import io.resys.hdes.client.api.programs.ImmutableProgramWrapper;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;
import io.resys.hdes.client.spi.ImmutableProgramContext;
import io.resys.hdes.client.spi.decision.DecisionProgramBuilder;
import io.resys.hdes.client.spi.decision.DecisionProgramExecutor;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CreateTaskTest extends TaskEnvirSetup {
  @Container @ServiceConnection static PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:17");
  @BeforeAll static void beforeAll() { start(CONTAINER); }
  @AfterAll static void afterAll() { end(); }
  @Autowired TaskClient taskClient;
  @Autowired HdesClient hdesClient;
  @Autowired HdesTypesMapper hdesTypesMapper;
  
  private Duration atMost = Duration.ofMinutes(5);


  @Test
  public void createTask() {
    final var dt = createDt();
    
    
    final var user = BaseEnvir.FAKER.starTrek().character();
    final var email = user+"@resys.io";
    
    final var task = taskClient.taskBuilder()
      .userId(user, email)
      .createTask(ImmutableCreateTaskCommand.builder()
      .subject(BaseEnvir.FAKER.book().title())
      .build())
      .await().atMost(atMost);
    
    final var comment = taskClient.taskBuilder()
      .userId(user, email)
      .createTaskComment(ImmutableCreateTaskCommentCommand.builder()
        .external(true)
        .commentText(BaseEnvir.FAKER.chuckNorris().fact())
        .taskId(task.getId())
        .source(TaskCommentSource.FRONTDESK)
        .build())
      .await().atMost(atMost);

    final var assignee = taskClient.taskBuilder()
      .userId(user, email)
      .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
          .status(TaskStatus.REJECTED)
          .assignedUser("bob")
          .assignedUserEmail("bob@bob")
          .subject(task.getSubject())
          .build())
      .await().atMost(atMost);

    final var assignee_change = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedUser("sam")
            .assignedUserEmail("sam@sam")
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    
    
    final var status_change = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .status(TaskStatus.DELEGATED)
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    
    
    final var priority_change = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .priority(TaskPriority.HIGH)
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);

    final var diff_version_1 = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), task.getVersion())
        .await().atMost(atMost);
    final var diff_version_1_events = eval(dt, diff_version_1);
    log.info("Diff version 1: \r\n {}events: \r\n{}", diff_version_1.getLog(), diff_version_1_events);
    Assertions.assertEquals(1, diff_version_1_events.size());
    Assertions.assertTrue(diff_version_1_events.containsAll(Arrays.asList("TASK_CREATED")));
    
    
    
    final var diff_version_2 = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), comment.getVersion())
        .await().atMost(atMost);
    final var diff_version_2_events = eval(dt, diff_version_2);
    log.info("Diff version 2: \r\n {}events: \r\n{}", diff_version_2.getLog(), diff_version_2_events);
    Assertions.assertEquals(2, diff_version_2_events.size());
    Assertions.assertTrue(diff_version_2_events.containsAll(Arrays.asList("TASK_UPDATED", "EXTERNAL_COMMENT_ADDED")));
    
    
    
    final var diff_version_3 = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), assignee.getVersion())
        .await().atMost(atMost);
    final var diff_version_3_events = eval(dt, diff_version_3);
    log.info("Diff version 3: \r\n {}events: \r\n{}", diff_version_3.getLog(), diff_version_3_events);
    Assertions.assertEquals(3, diff_version_3_events.size());
    Assertions.assertTrue(diff_version_3_events.containsAll(Arrays.asList("TASK_UPDATED", "TASK_ASSIGNEE_UPDATED", "TASK_STATUS_UPDATED")));
    
    
    
    final var diff_version_4 = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), assignee_change.getVersion())
        .await().atMost(atMost);
    final var diff_version4_events = eval(dt, diff_version_4);
    log.info("Diff version 4: \r\n {}events: \r\n{}", diff_version_4.getLog(), diff_version4_events);
    Assertions.assertEquals(2, diff_version4_events.size());
    Assertions.assertTrue(diff_version4_events.containsAll(Arrays.asList("TASK_UPDATED", "TASK_ASSIGNEE_UPDATED")));
    
    
    
  }
  
  public ImmutableProgramWrapper<AstDecision, DecisionProgram> createDt() {
    final var decision = hdesClient.ast().commands(Arrays.asList(
        ImmutableAstCommand.builder().type(AstCommandValue.SET_NAME).value("task_events_template").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("0").value("path").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("0").value("STRING").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("1").value("op").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("1").value("STRING").build(),

        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_OUT).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("2").value("event").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("2").value("STRING").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//3
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("4").value(in("")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("5").value(in("add")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("6").value("TASK_CREATED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//7
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("8").value(in("/updated")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("9").value(in("replace")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("10").value("TASK_UPDATED").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//11
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("12").value(qin("/completed/*")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("13").value(in("replace")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("14").value("TASK_COMPLETED").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//15
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("16").value(qin("/description/*")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("17").value(in("replace")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("18").value("TASK_DESC_UPDATED").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//19
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("20").value(qin("/dueDate/*")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("21").value(in("replace")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("22").value("TASK_DUEDATE_UPDATED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//23
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("24").value(qin("/subject/*")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("25").value(in("replace")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("26").value("TASK_SUBJECT_UPDATED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//27
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("28").value(qin(
            "/assignedId/*",
            "/assignedUser/*",
            "/assignedUserEmail/*"
        )).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("29").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("30").value("TASK_ASSIGNEE_UPDATED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//31
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("32").value(qin("/keyWords/*")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("33").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("34").value("TASK_KEYWORDS_UPDATED").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//35
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("36").value(qin("/assignedRoles/*")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("37").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("38").value("TASK_ROLES_UPDATED").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//39
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("40").value(qin("/status")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("41").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("42").value("TASK_STATUS_UPDATED").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//43
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("44").value(qin("/priority")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("45").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("46").value("TASK_PRIORITY_UPDATED").build(),

        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//47              
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("48").value(qin("/comments/*/external/true")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("49").value(in("add")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("50").value("EXTERNAL_COMMENT_ADDED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//51
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("52").value(qin("/comments/*/external/false")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("53").value(in("add")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("54").value("INTERNAL_COMMENT_ADDED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//55
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("56").value(qin("/status/NEW")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("57").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("58").value("TASK_STATUS_UPDATED_TO_NEW").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//59
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("60").value(qin("/status/OPEN")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("61").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("62").value("TASK_STATUS_UPDATED_TO_OPEN").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//63
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("64").value(qin("/status/DELEGATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("65").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("66").value("TASK_STATUS_UPDATED_TO_DELEGATED").build()
        
        
    )).decision();
    

    
    final var result = ImmutableProgramWrapper.<AstDecision, DecisionProgram>builder()
        .id(decision.getName())
        .type(decision.getBodyType())
        .source(ImmutableAstSource.builder().id("").hash("").bodyType(decision.getBodyType()).build())
        .status(ProgramStatus.UP)
        .program(new DecisionProgramBuilder(hdesTypesMapper).build(decision))
        .build();
    
    return result;
  }
  
  private List<String> eval(ProgramWrapper<AstDecision, DecisionProgram> program, TaskDiff diff) {
    final var envir = ImmutableProgramEnvir.builder().tagName("junittest").putDecisionsByName(program.getId(), program).build();
    
    final var events = new HashSet<String>();
    for(final var diffValue : diff.getValues()) {
      final var ctx = ImmutableProgramContext.builder(hdesTypesMapper, envir, DI);
      ctx.map(Map.of(
          "path", diffValue.getPath(),
          "op", diffValue.getOp().operationName()
      ));
      final var result = DecisionProgramExecutor.run(program.getProgram().get(), ctx.build());
      final var newEvents = DecisionProgramExecutor.find(result).stream()
          .map(e -> e.get("event").toString())
          .toList();
      
      events.addAll(newEvents);
    }
    
    return new ArrayList<>(events);
  }
  private String in(String exp) {
    return "in[\"" + exp + "\"]";
  }
  private String qin(String ...exp) {
    final var next = Arrays.asList(exp).stream().map(e -> "\"" + e + "\"").toList().toArray(new String[] {});
    return "qin[" + String.join(",", next) + "]";
  }
}
