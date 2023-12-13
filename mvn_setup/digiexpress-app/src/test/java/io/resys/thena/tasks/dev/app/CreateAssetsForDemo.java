package io.resys.thena.tasks.dev.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.thena.tasks.dev.app.config.MigrationProfile;
import io.resys.thena.tasks.dev.app.config.TestCase;
import io.resys.thena.tasks.dev.app.mig.DialobMigration;
import io.resys.thena.tasks.dev.app.mig.DialobMigration.FormsAndRevs;
import io.resys.thena.tasks.dev.app.mig.HdesMigration;
import io.resys.thena.tasks.dev.app.mig.HdesMigration.HdesState;
import io.resys.thena.tasks.dev.app.mig.MigrationsDefaults;
import io.resys.thena.tasks.dev.app.mig.StencilMigration;
import io.resys.thena.tasks.dev.app.mig.WorkflowMigration;
import io.resys.thena.tasks.dev.app.mig.WorkflowMigration.CommandData;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.extern.slf4j.Slf4j;


@Disabled
@QuarkusTest
@Slf4j
@TestProfile(MigrationProfile.class)
public class CreateAssetsForDemo extends TestCase {
  private final Duration atMost = Duration.ofMillis(100000);
  private final ObjectMapper om = MigrationsDefaults.om;
  
  @Test
  public void test() throws IOException {
    final var client = createRepo("test-prj-1").await().atMost(atMost);

    final var siteState = StencilMigration.builder().build().execute();
    final var hdes = HdesMigration.builder().build().execute(client.getConfig().getHdes());
    final var dialob = DialobMigration.builder().build().execute();
    final var sysConfigCommand = WorkflowMigration.builder().build().execute(hdes, dialob, siteState);
    
    
    createStencilJson(siteState);
    createHdesJson(hdes);
    createSysConfigJson(sysConfigCommand);
    createDialobJson(dialob, sysConfigCommand);
  }
  

  private void createDialobJson(FormsAndRevs formsRev, CommandData data) throws IOException {
    final var dir = new File("src/test/resources/init");

    dir.mkdirs();
    for(final var service : data.getCommand().getServices()) {
      
      final var form = formsRev.getForms().stream().filter(f -> f.getId().equals(service.getFormId())).findFirst().get();
      final var formId = service.getFormId() + "_" + form.getName();
      
      final var target = new File(dir, "asset_dialob_" + formId + ".json");
      final var created = target.createNewFile();
      if(created) {
        final var writer = new BufferedWriter(new FileWriter(target));
        writer.write(om.writeValueAsString(form));
        writer.close();
      }
    }
    

  }
  
  
  private void createSysConfigJson(CommandData data) throws IOException {
    final var siteState = data.getCommand(); 
    final var dir = new File("src/test/resources/init");

    dir.mkdirs();
    {
      final var target = new File(dir, "asset_sysconfig.json");
      final var created = target.createNewFile();
      if(created) {
        final var writer = new BufferedWriter(new FileWriter(target));
        writer.write(om.writeValueAsString(siteState));
        writer.close();
      }
    }
    
    {
      final var target = new File(dir, "asset_log.txt");
      final var created = target.createNewFile();
      if(created) {
        final var writer = new BufferedWriter(new FileWriter(target));
        writer.write(data.getLog());
        writer.close();
      }
    }
  }
  
  
  private void createHdesJson(HdesState siteState) throws IOException {
    final var composer = new HdesComposerImpl(super.assets().getConfig().getHdes());
    composer.importTag(siteState.getAstTag());
    final var dir = new File("src/test/resources/init");
    final var target = new File(dir, "asset_hdes.json");
    
    dir.mkdirs();
    final var created = target.createNewFile();
    if(created) {
      final var writer = new BufferedWriter(new FileWriter(target));
      writer.write(om.writeValueAsString(siteState.getAstTag()));
      writer.close();
    }
  }
  
  private void createStencilJson(SiteState siteStateInit) throws IOException {
    final var composer = new StencilComposerImpl(super.assets().getConfig().getStencil());
    composer.migration().importData(siteStateInit).await().atMost(atMost);
    
    for(final var wk : siteStateInit.getWorkflows().values()) {
      if(Boolean.TRUE.equals(wk.getBody().getDevMode())) {
        composer.delete().workflow(wk.getId()).await().atMost(atMost);
      }
    }
    for(final var article : siteStateInit.getArticles().values()) {
      if(Boolean.TRUE.equals(article.getBody().getDevMode())) {
        composer.delete().article(article.getId()).await().atMost(atMost);
      }
    }
    
    final var siteState = super.assets().getConfig().getStencil().getStore().query().head().await().atMost(atMost);;
    
    final var dir = new File("src/test/resources/init");
    final var target = new File(dir, "asset_stencil.json");
    
    dir.mkdirs();
    final var created = target.createNewFile();
    if(created) {
      final var writer = new BufferedWriter(new FileWriter(target));

      final var result = ImmutableSiteState.builder()
          .from(siteState)
          .releases(Collections.emptyMap())
          .build();
      
      writer.write(om.writeValueAsString(result));
      writer.close();
    }
  }
}
