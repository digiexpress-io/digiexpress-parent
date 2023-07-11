package io.digiexpress.client.tests;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.digiexpress.client.api.AssetEnvir.ServiceProgramDef;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramDialob;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramHdes;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramStatus;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramStencil;
import io.digiexpress.client.api.AssetExecutorEntity.ProcessCreated;
import io.digiexpress.client.api.AssetExecutorEntity.ProcessState;
import io.digiexpress.client.api.AssetExecutorEntity.Step;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ImmutableCreateDescriptor;
import io.digiexpress.client.api.ImmutableCreateProjectRevision;
import io.digiexpress.client.api.ImmutableCreateRelease;
import io.digiexpress.client.spi.support.MainBranch;
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
        .tenant().repoProject("test-prj-1").create()
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
    final Project project = service(client).create().revision(ImmutableCreateProjectRevision.builder()
        .name("init")
        .description("first iterator process to handle general message")
        .build()).await().atMost(atMost);
    
    final DefinitionState defState = service(client).query().definition(project.getHeadDefId()).await().atMost(atMost);
    
    
    final var def = service(client).create().serviceDescriptor(ImmutableCreateDescriptor.builder()
        .name("process-for-bindig-gen-msg-to-task")
        .desc("process-desc")
        .flowId("case 1 flow")
        .formId("c89b6d0b-51a7-11bc-358d-3094ca98d40b")
        .defId(defState.getDefinition().getId())
        .defVersionId(defState.getDefinition().getVersion())
        .build()).await().atMost(atMost);
    
    final var targetDate = LocalDateTime.of(2022, 11, 5, 11, 07);
    
    final var release1 = service(client).create().release(ImmutableCreateRelease.builder()
        .name(MainBranch.HEAD_NAME).desc("")
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
    final var service = (ServiceProgramDef) envir.getById("main/PROJECT");    

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

  private String toHashes(ServiceRelease release) {
    final var values = release.getValues().stream()
      .sorted((a, b) -> a.getId().compareTo(b.getId()))
      .map(e -> e.getBodyType() + "/" + e.getBodyHash())
      .collect(Collectors.toList());
    
    return String.join(System.lineSeparator(), values);
  }
}
