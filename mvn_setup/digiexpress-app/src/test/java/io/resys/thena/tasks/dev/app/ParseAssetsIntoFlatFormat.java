package io.resys.thena.tasks.dev.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.sysconfig.client.mig.MigrationClient;
import io.resys.sysconfig.client.mig.visitors.DialobMigration;
import io.resys.sysconfig.client.mig.visitors.DialobMigration.FormsAndRevs;
import io.resys.sysconfig.client.mig.visitors.HdesMigration;
import io.resys.sysconfig.client.mig.visitors.HdesMigration.HdesState;
import io.resys.sysconfig.client.mig.visitors.StencilMigration;
import io.resys.sysconfig.client.mig.visitors.WorkflowMigration;
import io.resys.sysconfig.client.mig.visitors.WorkflowMigration.CommandData;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentUserRecord;
import io.resys.thena.tasks.dev.app.config.MigrationProfile;
import io.resys.thena.tasks.dev.app.config.TestCase;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilComposerImpl;
import jakarta.enterprise.inject.Produces;


@org.junit.jupiter.api.Disabled
@QuarkusTest
@TestProfile(MigrationProfile.class)
public class ParseAssetsIntoFlatFormat extends TestCase {
  private final Duration atMost = Duration.ofMillis(100000);
  private ObjectMapper om;
  
  @Produces
  public CurrentUser currentUserDev() {
    return new CurrentUserRecord("local-tester", "first name", "last-name", "first.last@digiexpress.io");
  }
  

  @Test
  public void testFlat() throws IOException {
    final var client = new MigrationClient(createRepo("test-prj-1").await().atMost(atMost), Map.of("", "Digi-Express"));
    
    final var flat = client.create().await().atMost(atMost);
    
    final var dir = new File("src/test/resources/init-flat");
    dir.mkdirs();
    
    {
      final var target = new File(dir, "asset_sysconfig_flat.json");
      final var created = target.createNewFile();
      if(created) {
        final var writer = new BufferedWriter(new FileWriter(target));
        writer.write(client.getOm().writeValueAsString(flat));
        writer.close();
      }
    }
  }
  
  
  @Test
  public void test() throws IOException {
    final var client = new MigrationClient(createRepo("test-prj-1").await().atMost(atMost), new HashMap<>());
    this.om = client.getOm();
    
    final var siteState = new StencilMigration(client.getFolder(), client.getOm()).execute();
    final var hdes = new HdesMigration(client.getFolder(), client.getOm()).execute(client.getAssets().getConfig().getHdes());
    final var dialob = new DialobMigration(client.getFolder(), client.getOm()).execute();
    final var sysConfigCommand = new WorkflowMigration(client.getFolder(), client.getOm()).execute(hdes, dialob, siteState);
    
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
