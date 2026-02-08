package io.resys.thena.fs.spi.snapshot;

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Ref;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class MergeBranchImpl {

  private final Ref ref;
  private final Commit commit; 
  
  public MergeBranchResult close() {
    
  }
  
  @Value
  public static class MergeBranchResult {
    Ref branch;
  }
}
