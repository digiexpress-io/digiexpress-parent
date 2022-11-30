package io.digiexpress.client.tests;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.digiexpress.client.api.ImmutableCreateProcess;
import io.digiexpress.client.api.ImmutableCreateRelease;
import io.digiexpress.client.api.ImmutableCreateServiceRevision;
import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ProcessState.ProcessCreated;
import io.digiexpress.client.api.ProcessState.Step;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramDef;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramDialob;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramHdes;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStatus;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStencil;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.digiexpress.client.tests.support.PgProfile;
import io.digiexpress.client.tests.support.TestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(PgProfile.class)
public class IntegrationTest extends TestCase {
  private final Duration atMost = Duration.ofMillis(100000);
  
  @Test
  public void init() throws JsonProcessingException {
    
    final var builder = builder("test_cases/case_1/");

    // create stencil/dialob/wrench/process projects
    final var client = client()
        .repo().repoService("test-prj-1").create()
        .await().atMost(atMost);
    
    // create new form from file
    dialob(client).create(builder.reader().formDocument("case_1_form.json"))
        .onFailure().invoke(log()).await().atMost(atMost);
    
    // create site with one workflow('general-message')
    stencil(client).create().batch(builder.reader().content("case_1_content.json")).await().atMost(atMost);
    
    // create wrench service and flow
    hdes(client).create(builder.reader().flowService("case_1_service.txt")).await().atMost(atMost);
    hdes(client).create(builder.reader().flow("case_1_flow.txt")).await().atMost(atMost);

    // define service 
    final ServiceRevisionDocument revision = service(client).create().revision(ImmutableCreateServiceRevision.builder()
        .name("init")
        .description("first iterator process to handle general message")
        .build()).await().atMost(atMost);
    
    final var def = service(client).create().process(ImmutableCreateProcess.builder()
        .name("process-for-bindig-gen-msg-to-task")
        .desc("process-desc")
        .flowId("case 1 flow")
        .formId("c89b6d0b-51a7-11bc-358d-3094ca98d40b")
        .serviceRevisionId(revision.getId())
        .serviceRevisionVersionId(revision.getVersion())
        .build()).await().atMost(atMost);
    
    final var targetDate = LocalDateTime.of(2022, 11, 5, 11, 07);
    
    final var release1 = service(client).create().release(ImmutableCreateRelease.builder()
        .name(ServiceAssert.BRANCH_MAIN).desc("")
        .serviceDefinitionId(def.getId())
        .activeFrom(targetDate)
        .targetDate(targetDate)
        .build()).await().atMost(atMost);
  
    final var release2 = service(client).create().release(ImmutableCreateRelease.builder()
        .name("snapshot-clone").desc("")
        .serviceDefinitionId(def.getId())
        .activeFrom(LocalDateTime.of(2022, 11, 5, 11, 8))
        .targetDate(LocalDateTime.of(2022, 11, 5, 11, 8))
        .build()).await().atMost(atMost);
  
    // content hashes must be same, because there are no changes
    Assertions.assertEquals(toHashes(release1), toHashes(release2));
    
    
    final var envir = builder.getClient().envir().add(release1).add(release2).build();
    envir.getSources().values().forEach(src -> {
      Assertions.assertEquals(envir.getById(src.getId()), envir.getByHash(src.getHash()));
    });
    
    
    final var stencil = (ServiceProgramStencil) envir.getById("main/STENCIL");
    final var dialob = (ServiceProgramDialob) envir.getById("c89b6d0b-51a7-11bc-358d-3094ca98d40b/DIALOB");
    final var hdes = (ServiceProgramHdes) envir.getById("main/HDES");    
    final var service = (ServiceProgramDef) envir.getById("main/SERVICE");    

    Assertions.assertNotNull(stencil.getDelegate(builder.getClient().getConfig()));
    Assertions.assertNotNull(dialob.getCompiled(builder.getClient().getConfig()));
    Assertions.assertNotNull(hdes.getCompiled(builder.getClient().getConfig()));
    Assertions.assertNotNull(service.getDelegate(builder.getClient().getConfig()));
    
    Assertions.assertEquals(ServiceProgramStatus.UP, stencil.getStatus());
    Assertions.assertEquals(ServiceProgramStatus.UP, dialob.getStatus());
    Assertions.assertEquals(ServiceProgramStatus.UP, hdes.getStatus());
    Assertions.assertEquals(ServiceProgramStatus.UP, service.getStatus());
    
    final var stencilOnDate = client.executor(envir).stencil().build();
    final var workflow = stencilOnDate.getBody().getLinks().get("d6249d85647a72e9b9d8981f1c612b16");
    Assertions.assertNotNull(workflow);
    
    final ProcessState newProcess = client.executor(envir).process(workflow.getId())
        .action("firstName", "Sam")
        .action("lastName", "Vimes")
        .build().getBody();
    final Step<ProcessCreated> processCreated = newProcess.getStepProcessCreated(); 
    Assertions.assertNotNull(processCreated);
    Assertions.assertNotNull(processCreated.getBody().getParams().get("firstName"));
    Assertions.assertNotNull(processCreated.getBody().getParams().get("lastName"));    
    
    final var fill = fill(envir, client, newProcess);
    {
      // create and answer session questions
      fill.start();
      fill.answers().answerQuestion("mainCategory", "residence").build();
      fill.answers().answerQuestion("list1", "else").build();
      fill.answers().answerQuestion("Viesti", "my personal msg").build();
      fill.complete();
    }
    final ProcessState fillState = fill.getState();
    Assertions.assertNotNull(fillState.getStepFillCompleted());
    
    final var flow = client.executor(envir).hdes(fillState).store(getQuestionnaireStore()).build().getBody();
  
    log.debug(toJson(flow.getState()));
//    log.debug(toJson(release1));
//    log.debug(builder.print(client.getConfig().getStore()));
  }

  private String toHashes(ServiceReleaseDocument release) {
    final var values = release.getValues().stream()
      .sorted((a, b) -> a.getId().compareTo(b.getId()))
      .map(e -> e.getBodyType() + "/" + e.getBodyHash())
      .collect(Collectors.toList());
    
    return String.join(System.lineSeparator(), values);
  }
}
