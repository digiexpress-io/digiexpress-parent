package io.digiexpress.mig.client.api;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
public interface SourceThena {
  String getTenantPrefix();
  
  Map<String, io.resys.thena.api.entities.git.Blob> getBlobs();
  Map<String, io.resys.thena.api.entities.git.Commit> getCommits();
  List<io.resys.thena.api.entities.git.Branch> getBranches();
  List<io.resys.thena.api.entities.git.Tag> getTags();
  List<TreeValueExt> getTreeValues();
  List<io.resys.thena.api.entities.git.Tree> getTrees();
  
  @Value.Immutable
  interface TreeValueExt {
    String getTree();
    String getName();
    String getBlob();
  }
}
