package io.resys.sysconfig.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.model.ImmutableCreateSysConfig;
import io.resys.sysconfig.client.api.model.ImmutableCreateSysConfigDeployment;
import io.resys.sysconfig.client.api.model.ImmutableCreateSysConfigRelease;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigService;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.tests.config.SysConfigPgProfile;
import io.resys.sysconfig.client.tests.config.TestCase;
import io.resys.thena.projects.client.spi.store.MainBranch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(SysConfigPgProfile.class)
public class IntegrationTest extends TestCase {
  private final Duration atMost = Duration.ofMillis(100000);
  
  @Test
  public void init() throws JsonProcessingException {
    

    // create stencil/dialob/wrench/process projects
    final var client = createRepo("test-prj-1", "test_cases/case_1/").await().atMost(atMost);
    
    // create new form from file
    dialob().create(builder.reader().formDocument("case_1_form.json"))
        .onFailure().invoke(log()).await().atMost(atMost);
    
    // create site with one workflow('general-message')
    stencil().create().batch(builder.reader().content("case_1_content.json")).await().atMost(atMost);
    
    // create wrench service and flow
    hdes().create(builder.reader().flowService("case_1_service.txt")).await().atMost(atMost);
    hdes().create(builder.reader().flow("case_1_flow.txt")).await().atMost(atMost);

    // define service 
    final SysConfig project = sysConfig().createConfig()
        .createOne(ImmutableCreateSysConfig.builder()
            .targetDate(getTargetDate())
            .userId(getUserId())
            .name("system config to bind stencil-dialob-wrench")
            .wrenchHead(MainBranch.HEAD_NAME)
            .stencilHead(MainBranch.HEAD_NAME)
            .addServices(ImmutableSysConfigService.builder()
                .addLocales("en")
                .serviceName("process-for-bindig-gen-msg-to-task")
                .flowName("case 1 flow")
                .formId("c89b6d0b-51a7-11bc-358d-3094ca98d40b")
                .build())
            .build()).await().atMost(atMost);
    
    final SysConfigRelease release = sysConfig().createRelease().createOne(
          ImmutableCreateSysConfigRelease.builder()
          .id(project.getId())
          .targetDate(getTargetDate())
          .releaseName("test-release-v1")
          .build())
        .await().atMost(atMost);
    
    final var deployment = sysConfig().createDeployment().createOne(ImmutableCreateSysConfigDeployment.builder()
        .body(release)
        .liveDate(getTargetDate())
        .pushToLive(true)
        .userId(getUserId())
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);
    
    final var session = executor().createSession()
      .ownerId(getUserId())
      .addProp("firstName", "Sam")
      .addProp("lastName", "Vimes")
      .targetDate(getTargetDate())
      .locale("en")
      .workflowName("process-for-bindig-gen-msg-to-task")
      .build()
      .await().atMost(atMost);
    
    // Dialob
    {
      final var fill = fill(session);
      
      // create and answer session questions
      fill.start();
      fill.answers().answerQuestion("mainCategory", "residence").build();
      fill.answers().answerQuestion("list1", "else").build();
      fill.answers().answerQuestion("Viesti", "my personal msg").build();
      fill.complete();
      
      final SysConfigSession fillState = fill.getState();
      Assertions.assertNotNull(fillState.getState().getStepFillCompleted());
    }

    
    final SysConfigSession afterProcess = executor().processFillInstance()
      .session(session)
      .targetDate(getTargetDate())
      .build()
      .await().atMost(atMost);
      
    log.debug(toJson(afterProcess.getState()));
  }
}
