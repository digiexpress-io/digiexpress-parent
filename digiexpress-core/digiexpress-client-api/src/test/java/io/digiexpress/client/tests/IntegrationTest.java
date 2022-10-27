package io.digiexpress.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.digiexpress.client.tests.support.PgProfile;
import io.digiexpress.client.tests.support.TestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(PgProfile.class)
public class IntegrationTest extends TestCase {
  private final Duration atMost = Duration.ofMillis(400);
  
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
    
    // define process 
    
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
