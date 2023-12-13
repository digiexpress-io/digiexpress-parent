package io.resys.thena.tasks.dev.app.mig;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Builder
public class HdesMigration {
  @lombok.Builder.Default
  private final String src = MigrationsDefaults.folder + "hdes";
  @lombok.Builder.Default
  private final ObjectMapper om = MigrationsDefaults.om;
  
  @lombok.Data @lombok.Builder
  public static class HdesState {
    private StoreState storeState;
    private ProgramEnvir programEnvir;  
    private AstTag astTag;
  }
  
  public HdesState execute(HdesClient client) {
    final var dir = new File(src);
    SysConfigAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with hdes *AstTag.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary = MigrationsDefaults.summary("tag name", "asset name", "type", "status");
    final var tags = new ArrayList<AstTag>();
    StoreState state = null;
    ProgramEnvir envir = null;
    AstTag astTag = null;
    for(final var file : files) {
      try {
        final var input = new String(Files.newInputStream(file.toPath()).readAllBytes(), StandardCharsets.UTF_8);
        final AstTag tag = (AstTag) om.readValue(input, AstTag.class);
        tags.add(tag);
        summary.addRow(tag.getName(), tag.getName(), "TAG", "OK");
        
        final var store = HdesInMemoryStore.builder().build(tag);
        state = store.query().get().await().atMost(Duration.ofMinutes(1));
        final var envirBuilder = client.envir();
        for(final var entity : state.getDecisions().values()) {
          envirBuilder.addCommand().decision(entity).id(entity.getId()).build();
        }
        for(final var entity : state.getFlows().values()) {
          envirBuilder.addCommand().flow(entity).id(entity.getId()).build();
        }
        for(final var entity : state.getServices().values()) {
          envirBuilder.addCommand().service(entity).id(entity.getId()).build();
        }        
        
        envir = envirBuilder.build();
        for(final var wrapper : envir.getValues().values()) {
          summary.addRow(tag.getName(), wrapper.getAst().map(a -> a.getName()).orElse(wrapper.getId()), wrapper.getType(), wrapper.getStatus());          
        }
        astTag = tag;
      } catch (Exception e) {
        summary.addRow(file.getName(), file.getName(), "TAG", "FAIL");
        log.error("Reading hdes tags: '" + src + "', found: " + files.length + summary.toString());
        throw new RuntimeException("Failed to read hdes form json: " + file.getName() + ". " + e.getMessage(), e);
      }
    }
    
    log.info("Reading hdes tags: '" + src + "', found: " + files.length + summary.toString());
    return HdesState.builder().storeState(state).programEnvir(envir).astTag(astTag).build();
  }
  
  
}
