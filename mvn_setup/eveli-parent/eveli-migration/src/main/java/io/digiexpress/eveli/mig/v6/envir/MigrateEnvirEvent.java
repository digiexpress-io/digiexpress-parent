package io.digiexpress.eveli.mig.v6.envir;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrateEnvirEvent implements AssetEventMigration {
  private final FileSystem fs;
  private final AssetEvent event;
  
  public Uni<Void> execute() {
    
    
    return Uni.createFrom().voidItem();
  }
}
