package io.resys.thena.tasks.dev.app.mig;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Builder
public class StencilMigration {
  @lombok.Builder.Default
  private final String src = MigrationsDefaults.folder + "stencil";
  @lombok.Builder.Default
  private final ObjectMapper om = MigrationsDefaults.om;
  
  public SiteState execute() {
    final var dir = new File(src);
    SysConfigAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with stencil *release.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary = MigrationsDefaults.summary("file name", "stencil value", "type", "status");
    SiteState state = null;
    for(final var file : files) {
      try {
        state = om.readValue(file, ImmutableSiteState.class);
        
        state.getLocales().values().forEach(v -> 
          summary.addRow(file.getName(), v.getBody().getValue(), v.getType(), "OK")  
        );
        
        final var pages = state.getPages().values();
        final var locales = state.getLocales();
        state.getArticles().values().forEach(v -> {
          summary.addRow(file.getName(), v.getBody().getName(), v.getType(), "OK");
          pages.stream().filter(p -> p.getBody().getArticle().equals(v.getId())).forEach(p -> 
            summary.addRow(file.getName(), locales.get(p.getBody().getLocale()).getBody().getValue(), p.getType(), "OK")  
          );
        });

        state.getLinks().values().forEach(v -> 
          summary.addRow(file.getName(), v.getBody().getValue(), v.getType(), "OK")  
        );        
        state.getWorkflows().values()
        .stream()
        .filter(v -> !Boolean.TRUE.equals(v.getBody().getDevMode()))
        .forEach(v -> 
          summary.addRow(file.getName(), v.getBody().getValue(), v.getType(), "OK")  
        );
        
        summary.addRow(file.getName(), state.getName(), state.getContentType(), "OK");
      } catch (Exception e) {
        summary.addRow(file.getName(), "", "release", "FAIL");
        throw new RuntimeException("Failed to read stencil form json: " + file.getName() + ". " + e.getMessage(), e);
      }
    }
    log.info("Reading hdes tags: '" + src + "', found: " + files.length + summary.toString());
    return state;
  }
  
  
}
