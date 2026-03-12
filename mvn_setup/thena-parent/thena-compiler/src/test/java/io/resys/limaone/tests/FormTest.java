package io.resys.limaone.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import io.dialob.api.form.Form;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDbImpl;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.json.JsonObject;



@DialobTest( enabled = true )
public class FormTest {

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
  }
  
  private FormDb getFormDb(FormUrl formUrl) {
    final var restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(formUrl.getUrl() + "/api"));
    
    return FormDbImpl.builder()
        .restTemplate(restTemplate)
        .objectMapper(io.resys.thena.jackson.QuarkusJacksonJsonCodec.mapper())
        .build();
  }
}
