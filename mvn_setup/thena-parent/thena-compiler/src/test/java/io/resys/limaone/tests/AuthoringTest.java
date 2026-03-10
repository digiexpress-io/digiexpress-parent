package io.resys.limaone.tests;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AuthoringTest extends DbSupport {

  @Test
  public void createTest() {
    final var authoring = new AuthoringImpl(createConfig());
    
    final var template1 = authoring.newModel()
        .newArticleTemplate()
        .props(props -> props.name("Nice page template").content("# Header 1").type("Page").description("Generic page structure"))
        .buildSync();
        
    final var article1 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My first article").order(100))
        .buildSync();

    final var article2 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My second article").order(100))
        .buildSync();
    
   
    authoring.newModel()
      .newDeployment()
      .props(props -> props.name("v1.5").description("test release"))
      .buildSync();
   
    authoring.newModel()
      .newDeployment()
      .props(props -> props.name("v2.4").description("new content"))
      .buildSync();
   
   
    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    
    final var locale2 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("fi"))
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
    
    
    
    // create state
    // var expected = TestExporter.toString(getClass(), "create_state.txt");
    // var actual = super.toRepoExport("test1");
    // Assertions.assertEquals(expected, actual);
    
    authoring.modifyModel()
        .modifyArticleTemplate()
        .props(props -> props.templateId(template1.getId())
            .name("new name")
            .content("cool content")
            .type("PAGE")
            .description("description"))
        .buildSync();

    authoring.modifyModel()
        .modifyArticle()
        .props(props -> props.articleId(article1.getId()).name("Revised Article1").order(300))
        .buildSync();
    
    authoring.modifyModel()
        .modifyLocale()
        .props(props -> props.localeId(locale1.getId()).value("gb").enabled(false))
        .buildSync();
    
    authoring.modifyModel()
        .modifyArticlePage()
        .props(props -> props.pageId(page1.getId()).content("new content for page1").locale(locale1.getId()))
        .buildSync();
    
    authoring.modifyModel()
        .modifyArticleLink()
        .props(props -> props.linkId(link1.getId()).articles(Arrays.asList(article1.getId()))
            .value("www.wikipedia.com").type("external")
            .addLabels(ImmutableLocaleLabel.builder()
                .labelValue("Don't click me").locale(locale2.getId())
                .build()))
        .buildSync();
    
    authoring.modifyModel()
        .modifyArticleWorkflow()
        .props(props -> props.workflowId(workflow1.getId())
            .value("revision of firstForm")
            .addLabels(ImmutableLocaleLabel.builder()
                .locale(locale2.getId())
                .labelValue("First form part 2")
                .build()))
        .buildSync();
    
    
    // update state
    // expected = TestExporter.toString(getClass(), "update_state.txt");
    // actual = super.toRepoExport("test1");
    // Assertions.assertEquals(expected, actual);
    
    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(template1.getId()).bodyType(Model.BodyType.ARTICLE_TEMPLATE))
      .buildSync();

    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(page1.getId()).bodyType(Model.BodyType.ARTICLE_PAGE))
      .buildSync();
    
    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(page2.getId()).bodyType(Model.BodyType.ARTICLE_PAGE))
      .buildSync();
    
    
    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(link1.getId()).bodyType(Model.BodyType.ARTICLE_LINK))
      .buildSync();
  
    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(workflow1.getId()).bodyType(Model.BodyType.ARTICLE_WORKFLOW))
      .buildSync();
  
    
    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(article1.getId()).bodyType(Model.BodyType.ARTICLE))
      .buildSync();

    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(article2.getId()).bodyType(Model.BodyType.ARTICLE))
      .buildSync();
  
    authoring.deleteModel()
      .deleteAny()
      .props(props -> props.id(locale1.getId()).bodyType(Model.BodyType.LOCALE))
      .buildSync();
  
    // delete state
    // expected = TestExporter.toString(getClass(), "delete_state.txt");
    // actual = super.toRepoExport("test1");
    // Assertions.assertEquals(expected, actual);
  }
}
