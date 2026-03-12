package io.resys.limaone.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import io.dialob.api.form.Form;
import io.resys.limaone.spi.dialob.FormDbImpl;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.json.JsonObject;



@DialobTest( enabled = true )
public class FormTest {

  @SuppressWarnings("unused")
  @Test @DialobResetDB
  public void test(FormUrl formUrl) {
    final var formDb = getFormDb(formUrl);
    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    
    final var created = formDb.withTenant().createForm()
      .props(form).build()
      .await().atMost(Duration.ofMinutes(1));
    
    final var merged = formDb.withTenant().mergeForm()
      .props(created).build()
      .await().atMost(Duration.ofMinutes(1));
    
    final var tag = formDb.withTenant().createFormTag()
        .formName(created.getName()).formVersion("my-first-tag")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    final var tags = formDb.withTenant()
        .formTagQuery().findAll()
        .collect().asList()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, tags.size(), "Expected exactly 1 created tag");
    
    
    // form queries
    {
      final var form_byId = formDb.withTenant()
          .formQuery().formId(created.getId()).findOne()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(form_byId.isPresent(), "Expected exactly 1 created form by technical id");
    
      
      final var formByTag = formDb.withTenant()
        .formQuery().formTag(created.getName(), "my-first-tag").findOne()
        .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form by name and tag");
      
      
      final var allForms = formDb.withTenant()
          .formMetaQuery().findAll().collect().asList()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form");
    }
    
    // form queries without cache
    {
      formDb.getFormDbProps().getCache().evictAll();
      final var form_byId = formDb.withTenant()
          .formQuery().formId(created.getId()).findOne()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(form_byId.isPresent(), "Expected exactly 1 created form by technical id");
      
      
      formDb.getFormDbProps().getCache().evictAll();
      final var formByTag = formDb.withTenant()
        .formQuery().formTag(created.getName(), "my-first-tag").findOne()
        .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form by name and tag");
      
      
      formDb.getFormDbProps().getCache().evictAll();
      final var allForms = formDb.withTenant()
          .formMetaQuery().findAll().collect().asList()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form");
    }
    
    
  }
  
  private FormDbImpl getFormDb(FormUrl formUrl) {
    final var restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(formUrl.getUrl() + "/api"));
    
    return FormDbImpl.builder()
        .restTemplate(restTemplate)
        .objectMapper(io.resys.thena.jackson.QuarkusJacksonJsonCodec.mapper())
        .build();
  }
}
