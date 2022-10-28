package io.digiexpress.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.digiexpress.client.api.ImmutableCreateRevision;
import io.digiexpress.client.api.ImmutableRefIdValue;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.tests.support.PgProfile;
import io.digiexpress.client.tests.support.TestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(PgProfile.class)
public class IntegrationTest extends TestCase {
  private final Duration atMost = Duration.ofMillis(1000);
  
  @Test
  public void init() throws JsonProcessingException {
    final var builder = builder("test_cases/case_1/");

    // create stencil/dialob/wrench/process projects
    final var client = client()
        .repo().repoService("test-prj-1").create()
        .await().atMost(atMost);
    
    // create new form from file
    dialob(client).create(builder.reader().formDocument("case_1_form.json")).await().atMost(atMost);
    
    // create site with one workflow('general-message')
    stencil(client).create().batch(builder.reader().content("case_1_content.json")).await().atMost(atMost);
    
    // create wrench service and flow
    hdes(client).create(builder.reader().flowService("case_1_service.txt")).await().atMost(atMost);
    hdes(client).create(builder.reader().flow("case_1_flow.txt")).await().atMost(atMost);

    
    // define process 
    service(client).create().revision(ImmutableCreateRevision.builder()
        .name("general-message-process")
        .description("process to handle general message")
        .addValues(ImmutableRefIdValue.builder().type(ConfigType.DIALOB).refName("general-message-form").tagName("main").build())
        .addValues(ImmutableRefIdValue.builder().type(ConfigType.HDES).refName("case 1 flow").tagName("main").build())
        .build()).await().atMost(atMost);
    
    
    System.out.println(builder.print(client.getConfig().getStore()));

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
}
