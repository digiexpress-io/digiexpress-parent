package io.digiexpress.tagomi.api;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

public interface TagomiStore {
  TagomiStore withTenant(String repoId, String headName);
  BranchQuery queryBranches();
  UpsertBuilder upsertBuilder();
  TenantBuilder tenantBuilder();
  CurrentStateQuery currentStateQuery();
  
  interface BranchQuery {
    Uni<Optional<io.resys.thena.api.entities.git.Branch>> findOneBranch();
    Uni<List<TagomiContainer.Tag>> findAllTags();
  }
  
  interface CurrentStateQuery {
    Uni<TagomiContainer> getState();
    Uni<TagomiContainer> getStateByCommitId(String commitId);
    Uni<TagomiContainer> findAllStateObjectsById(List<String> ids, TagomiContainer.TagomiDocType type);
  }
  
  interface UpsertBuilder {
    <T extends TagomiContainer.IsTagomiObject> Uni<T> delete(T toBeDeleted);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> get(String blobId);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> save(T toBeSaved);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> create(T toBeSaved);
    Uni<List<? extends TagomiContainer.IsTagomiObject>> saveAll(List<TagomiContainer.IsTagomiObject> toBeSaved);
    Uni<List<? extends TagomiContainer.IsTagomiObject>> batch(BatchCommand batch);
  }
  
  
  interface TenantBuilder {
    TenantBuilder tenantName(String repoName);
    TenantBuilder headName(String headName);
    Uni<TagomiStore> create();    
    TagomiStore build();
    Uni<Tuple2<Boolean, TagomiStore>> createIfNot();
  }
  
  @Value.Immutable
  interface BatchCommand {
    List<TagomiContainer.IsTagomiObject> getToBeCreated();
    List<TagomiContainer.IsTagomiObject> getToBeSaved();
    List<TagomiContainer.IsTagomiObject> getToBeDeleted();
  }

}
