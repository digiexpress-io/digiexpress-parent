package io.digiexpress.client.tests.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.spi.support.ServiceAssert;
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
  
  @lombok.Data @lombok.Builder
  public static class FormsAndRevs {
    private List<FormRevisionDocument> revs;
    private List<FormDocument> forms;
  }
  
  public List<SiteState> execute() {
    final var dir = new File(src);
    ServiceAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with stencil *release.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary = MigrationsDefaults.summary("file name", "stencil value", "type", "status");
    final List<SiteState> result = new ArrayList<>();
    for(final var file : files) {
      try {
        final var state = om.readValue(file, ImmutableSiteState.class);
        
        state.getLocales().values().forEach(v -> 
          summary.addRow(file.getName(), v.getBody().getValue(), v.getType(), "OK")  
        );          
        state.getArticles().values().forEach(v -> {
          summary.addRow(file.getName(), v.getBody().getName(), v.getType(), "OK");
          
          state.getPages().values().stream().filter(p -> p.getBody().getArticle().equals(v.getId())).forEach(p -> 
            summary.addRow(file.getName(), state.getLocales().get(p.getBody().getLocale()).getBody().getValue(), p.getType(), "OK")  
          );
        });

        state.getLinks().values().forEach(v -> 
          summary.addRow(file.getName(), v.getBody().getValue(), v.getType(), "OK")  
        );        
        state.getWorkflows().values().forEach(v -> 
          summary.addRow(file.getName(), v.getBody().getValue(), v.getType(), "OK")  
        );
        
        summary.addRow(file.getName(), state.getName(), state.getContentType(), "OK");
      } catch (Exception e) {
        summary.addRow(file.getName(), "", "release", "FAIL");
        throw new RuntimeException("Failed to read stencil form json: " + file.getName() + ". " + e.getMessage(), e);
      }
    }
    log.info("Reading hdes tags: '" + src + "', found: " + files.length + summary.toString());
    return result;
  }
  
  
}
