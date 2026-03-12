package io.resys.limaone.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.tests.support.DbSupport;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.json.JsonObject;



@DialobTest( enabled = true )
public class WorkflowTest extends DbSupport {

  
  @Test @DialobResetDB
  public void testAll(FormUrl formUrl) {
    
    final var formDb = TestTemplate.getFormDb(formUrl);
    setUpData(formDb);
    
    
    final var authoring = new AuthoringImpl(createConfig(formDb));
    final var compiler = CompilerImpl.builder()
      .astParser(authoring.getConfig().getAstParser())
      .build();
    
    // magic asset resource... has all we need to run dialob/wrench/stencil
    final var world = authoring.worldQuery().findAllSync();
    final var bundle = compiler.compile(world);
  }
  
  @SuppressWarnings("unused")
  public void setUpData(FormDb formDb) {
    
    
    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));
    
    final var tag = formDb.withTenant().createFormTag()
        .formName(created.getName()).formVersion("my-first-tag")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    
    final var authoring = new AuthoringImpl(createConfig());
    final var article = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("my_first_article").order(100))
        .buildSync();
    
    final var locale = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    

    final var page = authoring.newModel()
        .newArticlePage()
        .props(props -> props.articleId(article.getId()).locale(locale.getId()).content("# English content"))
        .buildSync();
    
    final var workflow = authoring.newModel()
        .newArticleWorkflow().props(props -> props
          .value("form-1")
          .formName(form.getName()).formTag(tag.getName()).flowName("flow1")
          .formId("external-form-id")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale.getId()).labelValue("firstForm").build())
          .build())
        .buildSync();
  }
}
