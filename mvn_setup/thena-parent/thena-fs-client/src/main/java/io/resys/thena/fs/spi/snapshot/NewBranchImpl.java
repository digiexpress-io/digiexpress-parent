package io.resys.thena.fs.spi.snapshot;

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.Ref;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class NewBranchImpl {

  private final String branchName;
  private final Commit commit; 
  
  public NewBranchResult close() {
    final var newRef = ImmutableRef.builder()
        .refName(branchName)
        .commitId(commit.getId())
        .build();
    return new NewBranchResult(newRef);
  }
  
  @Value
  public static class NewBranchResult {
    Ref branch;
  }
}
