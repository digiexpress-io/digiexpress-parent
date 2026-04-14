package io.resys.limaone.tests;

import org.junit.jupiter.api.Test;

import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class WorldFs_1_Test  extends DbSupport {

  @Test
  public void createSimpleFileSystem() {
    final var authoring = new AuthoringImpl(createConfig());
    createLocalesArticlesLinks(authoring);
    
    
    final var fileSystem = authoring.worldFsQuery().findAllSync();
    log.debug("Current FS: {}", fileSystem);
  }
  
  
  private void createLocalesArticlesLinks(Authoring authoring) {
    
    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    
    final var locale2 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("fi"))
        .buildSync();
    
    
    final var article1 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My first article").order(100))
        .buildSync();
    
    final var page1 = authoring.newModel()
        .newArticlePage()
        .props(props -> props.articleId(article1.getId()).locale(locale1.getId()).content("# English content"))
        .buildSync();
    
    final var page2 = authoring.newModel()
      .newArticlePage()
      .props(props -> props.articleId(article1.getId()).locale(locale2.getId()).content("# Finnish content"))
      .buildSync();
    
    
    final var link1 = authoring.newModel()
        .newArticleLink().props(props -> props.type("internal").value("www.example.com")
        .addLabels(ImmutableLocaleLabel.builder()
            .locale(locale1.getId()).labelValue("click me")
            .build())
      ).buildSync();
    
    final var workflow1 = authoring.newModel()
        .newArticleWorkflow().props(props -> props.value("Form1")
          .formName("form1").formTag("v1").flowName("flow1").formId("external-form-id")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("firstForm").build())
          .build()
      ).buildSync();
  }
}
