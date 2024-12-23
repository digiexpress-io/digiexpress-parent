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
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
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
            .wrenchHead(DocObjectsQueryImpl.BRANCH_MAIN)
            .stencilHead(DocObjectsQueryImpl.BRANCH_MAIN)
            .tenantId(builder.getTenant().getId())
            .addServices(ImmutableSysConfigService.builder()
                .addLocales("en")
                .serviceName("process-for-bindig-gen-msg-to-task")
                .flowName("case 1 flow")
                .formId("c89b6d0b-51a7-11bc-358d-3094ca98d40b")
                .build())
            .build())
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
        .await().atMost(atMost);
    
    final SysConfigRelease release = sysConfig().createConfig().createOne(
          ImmutableCreateSysConfigRelease.builder()
          .id(project.getId())
          .userId(getUserId())
          .targetDate(getTargetDate())
          .releaseName("test-release-v1")
          .scheduledAt(getTargetDate())
          .build())
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
        .await().atMost(atMost);
    
    final var deployment = sysConfig().createConfig().createOne(ImmutableCreateSysConfigDeployment.builder()
        .deploymentId("live-deploy")
        .body(release)
        .liveDate(getTargetDate())
        .userId(getUserId())
        .targetDate(getTargetDate())
        .build())
    .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
    .await().atMost(atMost);
    
    final var session = executor().createSession()
      .releaseId(deployment.getBody().getId())
      .ownerId(getUserId())
      .addProp("firstName", "Sam")
      .addProp("lastName", "Vimes")
      .targetDate(getTargetDate())
      .locale("en")
      .workflowName("process-for-bindig-gen-msg-to-task")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
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
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(atMost);
    
    Assertions.assertNotNull(afterProcess.getState().getStepFlowCompleted());
    Assertions.assertEquals("Tero Testi Äyrämö", afterProcess.getState().getStepFlowCompleted().get().getBody().getReturns().get("fullName"));
    
    log.debug(toJson(afterProcess.getState()));
  }
}
