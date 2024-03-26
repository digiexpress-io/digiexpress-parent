package io.resys.thena.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.ThenaDocObject.Doc;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocCommit;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface DocCommitActions {
  CreateOneDoc createOneDoc();
  CreateManyDocs createManyDocs();
  
  CreateOneDocBranch branchOneDoc();

  ModifyOneDoc modifyOneDoc();
  ModifyManyDocs modifyManyDocs();
  
  ModifyOneDocBranch modifyOneBranch();
  ModifyManyDocBranches modifyManyBranches();

  /*
   * Create doc and doc by: One/Many
   */
  interface CreateOneDoc {
    CreateOneDoc branchName(String branchName);           // first branch of the document, when empty generated as 'main' by the system 
    CreateOneDoc docId(@Nullable String docId);           // when empty generated by system
    CreateOneDoc parentDocId(@Nullable String parentDocId); 
    CreateOneDoc externalId(@Nullable String externalId); // user given unique id
    CreateOneDoc docType(String docType);                 // user given classifier
    CreateOneDoc ownerId(@Nullable String ownerId);       //user given 'grouping' identifier for claiming ownership  
    CreateOneDoc append(JsonObject doc);
    CreateOneDoc author(String author);
    CreateOneDoc message(String message);
    CreateOneDoc meta(@Nullable JsonObject docMeta);
    CreateOneDoc log(@Nullable JsonObject doc);
    Uni<OneDocEnvelope> build();
  }
  interface CreateManyDocs {
    CreateManyDocs branchName(String branchName);           // first branch of the document, when empty generated as 'main' by the system 
    CreateManyDocs docType(String docType);                 // user given classifier
    CreateManyDocs author(String author);
    CreateManyDocs message(String message);
    AddItemToCreateDoc item();
    Uni<ManyDocsEnvelope> build();
  }
  interface AddItemToCreateDoc {
    AddItemToCreateDoc parentDocId(@Nullable String parentDocId); 
    AddItemToCreateDoc docId(@Nullable String docId); 
    AddItemToCreateDoc meta(@Nullable JsonObject docMeta);
    AddItemToCreateDoc log(@Nullable JsonObject doc);
    AddItemToCreateDoc externalId(@Nullable String externalId);
    AddItemToCreateDoc ownerId(@Nullable String ownerId);
    AddItemToCreateDoc append(JsonObject doc);
    CreateManyDocs next();
  }

  /*
   * Modify/Delete existing doc by: One/Many 
   */
  interface ModifyOneDoc {
    ModifyOneDoc docId(String docIdOrExternalId);
    ModifyOneDoc author(String author);
    ModifyOneDoc message(String message);
    
    ModifyOneDoc meta(@Nullable JsonObject docMeta);
    ModifyOneDoc log(@Nullable JsonObject doc);
    ModifyOneDoc remove();                              
    Uni<OneDocEnvelope> build();
  }
  interface ModifyManyDocs {
    ModifyManyDocs author(String author);
    ModifyManyDocs message(String message);
    AddItemToModifyDoc item();
    Uni<ManyDocsEnvelope> build();
  }
  interface AddItemToModifyDoc {
    AddItemToModifyDoc docId(String docId);
    AddItemToModifyDoc meta(@Nullable JsonObject docMeta);
    AddItemToModifyDoc log(@Nullable JsonObject doc);
    AddItemToModifyDoc message(String message);
    AddItemToModifyDoc remove();
    ModifyManyDocs next();
  }
  
  
  
  interface CreateOneDocBranch {
    CreateOneDocBranch docId(String docId);
    CreateOneDocBranch branchFrom(@Nullable String branchIdFromWhatToCreateABranch);  // branch name from what to create the branch
    CreateOneDocBranch branchName(String branchName);              // must be provided by the user, new branch
    CreateOneDocBranch append(@Nullable JsonObject doc);           // when empty source branch content
    CreateOneDocBranch author(String author);
    CreateOneDocBranch message(String message);
    CreateOneDocBranch log(@Nullable JsonObject doc);
    Uni<OneDocEnvelope> build();
  }
  
  interface ModifyOneDocBranch {
    ModifyOneDocBranch docId(String docId);
    ModifyOneDocBranch branchName(String branchName);
    ModifyOneDocBranch parent(String versionToModify);
    ModifyOneDocBranch parentIsLatest();

    ModifyOneDocBranch append(JsonObject doc);
    ModifyOneDocBranch merge(JsonObjectMerge doc);
    ModifyOneDocBranch log(JsonObject doc);
    ModifyOneDocBranch remove(); // deletes the branch
    
    ModifyOneDocBranch author(String author);
    ModifyOneDocBranch message(String message);
    Uni<OneDocEnvelope> build();
  }
  
  interface ModifyManyDocBranches {
    int getItemsAdded();
    
    ModifyManyDocBranches author(String author);
    ModifyManyDocBranches message(String message);
    ModifyManyDocBranches branchName(String branchName);
    AddItemToModifyDocBranch item();
    Uni<ManyDocsEnvelope> build();
  }
  
  interface AddItemToModifyDocBranch {
    AddItemToModifyDocBranch branchName(String branchName);
    AddItemToModifyDocBranch parent(String versionToModify);
    AddItemToModifyDocBranch parentIsLatest();
    AddItemToModifyDocBranch message(String message);
    AddItemToModifyDocBranch append(JsonObject doc);
    AddItemToModifyDocBranch merge(JsonObjectMerge doc);
    AddItemToModifyDocBranch log(JsonObject doc);
    AddItemToModifyDocBranch remove(); // deletes the branch
    AddItemToModifyDocBranch docId(String docId);
    
    ModifyManyDocBranches next();
  }
  
  
  @Value.Immutable
  interface OneDocEnvelope extends ThenaEnvelope {
    String getRepoId();
    @Nullable Doc getDoc();
    @Nullable DocBranch getBranch();
    @Nullable DocCommit getCommit();
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
  @Value.Immutable
  interface ManyDocsEnvelope extends ThenaEnvelope {
    String getRepoId();
    List<Doc> getDoc();
    List<DocBranch> getBranch();
    List<DocCommit> getCommit();
    
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
}
