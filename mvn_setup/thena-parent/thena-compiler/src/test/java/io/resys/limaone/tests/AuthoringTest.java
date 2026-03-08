package io.resys.limaone.tests;

import org.junit.jupiter.api.Test;

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
        /*
    final var article1 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My first article").order(100))
        .buildSync();

    final var article2 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My second article").order(100).build())
        .buildSync();*/
    
   /*
   repo.create().release(
       ImmutableCreateRelease.builder().name("v1.5").note("test release").build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
   
   repo.create().release(
       ImmutableCreateRelease.builder().name("v2.4").note("new content").build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
   
    Entity<Locale> locale1 = repo.create().locale(
        ImmutableCreateLocale.builder().locale("en").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Locale> locale2 = repo.create().locale(
        ImmutableCreateLocale.builder().locale("fi").build()
      ).await().atMost(Duration.ofMinutes(1));
    
    Entity<Page> page1 = repo.create().page(
        ImmutableCreatePage.builder().articleId(article1.getId()).locale(locale1.getId()).content("# English content").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.create().page(
        ImmutableCreatePage.builder().articleId(article1.getId()).locale(locale2.getId()).content("# Finnish content").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Link> link1 = repo.create().link(
        ImmutableCreateLink.builder().type("internal").value("www.example.com")
        .addLabels(ImmutableLocaleLabel.builder()
            .locale(locale1.getId()).labelValue("click me")
            .build())
        .build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Workflow> workflow1 = repo.create().workflow( 
        ImmutableCreateWorkflow.builder().value("Form1")
          .formName("form1").formTag("v1").flowName("flow1").formId("external-form-id")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("firstForm").build())
          .build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    // create state
    var expected = TestExporter.toString(getClass(), "create_state.txt");
    var actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    repo.update().template(ImmutableTemplateMutator.builder().templateId(template1.getId())
      .name("new name")
      .content("cool content")
      .type("PAGE")
      .description("description")
      .build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    repo.update().article(ImmutableArticleMutator.builder().articleId(article1.getId()).name("Revised Article1").order(300).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().locale(ImmutableLocaleMutator.builder().localeId(locale1.getId()).value("gb").enabled(false).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().page(ImmutablePageMutator.builder().pageId(page1.getId()).content("new content for page1").locale(locale1.getId()).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().link(ImmutableLinkMutator.builder()
          .linkId(link1.getId()).articles(Arrays.asList(article1.getId()))
          .value("www.wikipedia.com").type("external")
          .addLabels(ImmutableLocaleLabel.builder()
              .labelValue("Don't click me").locale(locale2.getId())
              .build())
          .build())
          
    .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().workflow(ImmutableWorkflowMutator.builder()
        .workflowId(workflow1.getId())
        .value("revision of firstForm")
        .addLabels(ImmutableLocaleLabel.builder()
            .locale(locale2.getId())
            .labelValue("First form part 2")
            .build())
        .build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    
    // update state
    expected = TestExporter.toString(getClass(), "update_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    repo.delete().template(template1.getId())
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    repo.delete().page(page1.getId())
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    
    repo.delete().article(article1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().article(article2.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().locale(locale1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().link(link1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().workflow(workflow1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    // delete state
    expected = TestExporter.toString(getClass(), "delete_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);*/
  }
}
