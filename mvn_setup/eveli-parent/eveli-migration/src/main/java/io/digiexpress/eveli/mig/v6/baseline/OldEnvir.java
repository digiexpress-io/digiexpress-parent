package io.digiexpress.eveli.mig.v6.baseline;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Value;

public interface OldEnvir {

  Uni<OldEnvirObjects> findAll(String tenanPrefix);  

  @Value
  static class OldEnvirObjects {
    Tenant tenant;
    List<Doc> docs;
    List<DocBranch> branches; 
    List<DocCommit> commits; 
    
    public DocCommit getCommit(Doc doc) {
      return this.getCommits().stream()
          .filter(e -> e.getId().equals(doc.getCreatedWithCommitId()))
          .findFirst().get();
    }
  }
  
  @Value
  @Builder
  static class Doc {
    String id;
    String commitId;
    String createdWithCommitId;
    Optional<String> externalId;
    Optional<String> ownerId;
    Optional<String> docParentId;
    String docType;
    String docStatus;
    Optional<OffsetDateTime> docStartsAt;
    Optional<OffsetDateTime> docEndsAt;
    Optional<String> docName;
    Optional<String> docDescription;
    Optional<String> docSubStatus;
    Optional<JsonObject> docMeta;
  }
  @Value
  @Builder
  public class DocCommit {
    String id;
    Optional<String> branchId;
    String docId;
    OffsetDateTime createdAt;
    String author;
    String message;
    String commitLog;
    Optional<String> parent;
  }
  @Value
  @Builder
  static class DocBranch {
    String docId;
    String branchId;
    String commitId;
    String createdWithCommitId;
    String branchName;
    String branchStatus;
    JsonObject value;
    Optional<OffsetDateTime> valueStartsAt;
    Optional<OffsetDateTime> valueEndsAt;
    Optional<String> valueName;
    Optional<String> valueDescription;
    Optional<String> valueStatus;
  }
}
