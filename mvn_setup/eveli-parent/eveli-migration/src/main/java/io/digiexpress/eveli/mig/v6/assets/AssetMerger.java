package io.digiexpress.eveli.mig.v6.assets;

import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import lombok.Value;

public class AssetMerger {
  private OldGit.OldGitObjects stencil;
  
  
  public AssetMerger stencil(OldGit.OldGitObjects stencil) {
    this.stencil = stencil;
    return this;
  }
  
  public AssetMergerResult build() {
    
    new OldGitExtractor(stencil).build();
    
    return new AssetMergerResult();
  }
  
  

  
  @Value
  public static class AssetMergerResult {
    
  }
}
