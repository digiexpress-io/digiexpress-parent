package io.resys.sysconfig.client.mig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.model.ImmutableCreateSysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.mig.model.ImmutableMigrationAssets;
import io.resys.sysconfig.client.mig.model.MigrationAssets;
import io.resys.sysconfig.client.mig.visitors.DialobMigration;
import io.resys.sysconfig.client.mig.visitors.DialobMigration.FormsAndRevs;
import io.resys.sysconfig.client.mig.visitors.HdesMigration;
import io.resys.sysconfig.client.mig.visitors.HdesMigration.HdesState;
import io.resys.sysconfig.client.mig.visitors.StencilMigration;
import io.resys.sysconfig.client.mig.visitors.WorkflowMigration;
import io.resys.sysconfig.client.mig.visitors.WorkflowMigration.CommandData;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrationClient {
  private final ObjectMapper om = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  private final String folder = "src/test/resources/migration/"; 
  private final AssetClient assets;
  private final Map<String, String> scrub; 

  
  public Optional<MigrationAssets> read(String fileName){
    try {
      final var resource = this.getClass().getClassLoader().getResource(fileName);
      final File file = new File(resource.getFile());
      return Optional.of(om.readValue(file, MigrationAssets.class));
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public SiteState readStencil(MigrationAssets assets){
    try {
      return om.readValue(assets.getStencil(), SiteState.class);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public List<FormDocument> readDialob(MigrationAssets assets){
    try {
      final List<FormDocument> result = new ArrayList<>();
      for(final var form : assets.getForms()) {
        result.add(om.readValue(form, FormDocument.class));
      }
      return result;
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public AstTag readHdes(MigrationAssets assets){
    try {
      return om.readValue(assets.getHdes(), AstTag.class);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
    
  
  public Uni<MigrationAssets> create() throws IOException {
    final var siteState = new StencilMigration(folder, om).execute();
    final var hdes = new HdesMigration(folder, om).execute(assets.getConfig().getHdes());
    final var dialob = new DialobMigration(folder, om).execute();
    final var sysConfigCommand = new WorkflowMigration(folder, om).execute(hdes, dialob, siteState);
   
    Uni<String> stencil = createStencilJson(siteState);
    Uni<String> wrench = createHdesJson(hdes);
    Uni<String> sysConfig = createSysConfigJson(sysConfigCommand);
    Uni<List<String>> forms = createDialobJson(dialob, sysConfigCommand);
    
    return Uni.combine().all().unis(stencil, wrench, sysConfig, forms).asTuple()
        .onItem().transform(tuple -> {
          final var stencilString = tuple.getItem1();
          final var wrenchString = tuple.getItem2();
          final var sysConfigString = tuple.getItem3();
          final var formsString = tuple.getItem4();
          
          try {
            return ImmutableMigrationAssets.builder()
                .command(om.readValue(sysConfigString, ImmutableCreateSysConfig.class))
                .stencil(stencilString)
                .hdes(wrenchString)
                .forms(formsString)
                .build();
          } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        });
  }
  


  private Uni<List<String>> createDialobJson(FormsAndRevs formsRev, CommandData data) throws IOException {
    final var result = new ArrayList<String>();
    for(final var service : data.getCommand().getServices()) {
      final var form = formsRev.getForms().stream().filter(f -> f.getId().equals(service.getFormId())).findFirst().get();
      result.add(clean(form));
    }
    
    return Uni.createFrom().item(result);
  }
  
  
  private Uni<String> createSysConfigJson(CommandData data) throws IOException {
    return Uni.createFrom().item(clean(data.getCommand()));
  }
  
  
  private Uni<String> createHdesJson(HdesState siteState) throws IOException {
    //final var composer = new HdesComposerImpl(client.getConfig().getHdes());
    //composer.importTag(siteState.getAstTag());
    return Uni.createFrom().item(clean(siteState.getAstTag()));
  }
  
  private Uni<String> createStencilJson(SiteState siteStateInit) throws IOException {
    final var composer = new StencilComposerImpl(assets.getConfig().getStencil());
    return composer.migration().importData(siteStateInit)
        .onItem().transformToUni(imported -> {
    
          UniJoin.Builder<Object> builder = Uni.join().builder();
          
          for(final var wk : siteStateInit.getWorkflows().values()) {
            if(Boolean.TRUE.equals(wk.getBody().getDevMode())) {
              final Uni<Object> command = composer.delete().workflow(wk.getId())
                  .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
                  .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
              builder.add(command);
            }
          }
          for(final var article : siteStateInit.getArticles().values()) {
            if(Boolean.TRUE.equals(article.getBody().getDevMode())) {
              final Uni<Object> command = composer.delete().article(article.getId())
                  .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
                  .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
              builder.add(command);
            }
          }
          
          return builder.joinAll().andFailFast();
        })
        .onItem().transformToUni(junk -> assets.getConfig().getStencil().getStore().query().head())
        .onItem().transform(state -> clean(state));   
  }
  
  private String clean(CreateSysConfig init) {
    try {
      var stringValue = this.om.writeValueAsString(init);
      
      return replace(stringValue);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  private String clean(FormDocument init) {
    try {
      var stringValue = this.om.writeValueAsString(init);
      
      return replace(stringValue);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  private String clean(SiteState init) {
    try {
      final var prepForScrubbing = ImmutableSiteState.builder().from(init).releases(Collections.emptyMap()).build();
      var stringValue = this.om.writeValueAsString(prepForScrubbing);
      
      return replace(stringValue);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    } 
  }
  
  private String clean(AstTag init) {
    try {
      var stringValue = this.om.writeValueAsString(init);
      
      return replace(stringValue);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  
  private String replace(String src) {
    var result = src;
    for(final var entry : this.scrub.entrySet()) {
      result = result.replaceAll("(?i)" + entry.getKey(), entry.getValue());
    }
    return result;
  }

  public ObjectMapper getOm() {
    return om;
  }
  public String getFolder() {
    return folder;
  }
  public AssetClient getAssets() {
    return assets;
  }
}
