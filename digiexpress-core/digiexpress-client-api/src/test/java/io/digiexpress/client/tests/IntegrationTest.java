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
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.tests.support.PgProfile;
import io.digiexpress.client.tests.support.TestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

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
        .name("snapshot").desc("")
        .serviceDefinitionId(def.getId())
        .activeFrom(targetDate)
        .targetDate(targetDate)
        .build()).await().atMost(atMost);
  
    final var release2 = service(client).create().release(ImmutableCreateRelease.builder()
        .name("snapshot-clone").desc("")
        .serviceDefinitionId(def.getId())
        .activeFrom(targetDate)
        .targetDate(targetDate)
        .build()).await().atMost(atMost);
  
    // content hashes must be same, because there are no changes
    Assertions.assertEquals(toHashes(release1), toHashes(release2));
    
    
    System.out.println(toJson(release1));
    System.out.println(builder.print(client.getConfig().getStore()));
    
    //client.envir().from(null);
    
    //        .addValues(ImmutableRefIdValue.builder().type(ConfigType.DIALOB).refName("general-message-form").tagName("main").build())
    // .addValues(ImmutableRefIdValue.builder().type(ConfigType.HDES).refName("case 1 flow").tagName("main").build())
  }
  
  
//  @Test
//  public void tojson() throws JsonProcessingException {
//    ImmutableBatchSite site = ImmutableBatchSite.builder()
//    .addLocales(ImmutableCreateLocale.builder().locale("en").build())
//    .addArticles(ImmutableCreateArticle.builder().name("index").build())
//    .addPages(ImmutableCreatePage.builder().content("# This is opening page").articleId("index").locale("en").build())
//    .addWorkflows(ImmutableCreateWorkflow.builder().addArticles("index").value("general-message").addLabels(ImmutableLocaleLabel.builder().locale("en").labelValue("send us a message using a form").build()).build())
//    .build();
//    
//    final var res = new ObjectMapper().writeValueAsString(site);
//    
//    System.out.println(res);
//  }
  
  
  private String toHashes(ServiceReleaseDocument release) {
    final var values = release.getValues().stream()
      .sorted((a, b) -> a.getId().compareTo(b.getId()))
      .map(e -> e.getBodyType() + "/" + e.getBodyHash())
      .collect(Collectors.toList());
    
    return String.join(System.lineSeparator(), values);
  }
}
