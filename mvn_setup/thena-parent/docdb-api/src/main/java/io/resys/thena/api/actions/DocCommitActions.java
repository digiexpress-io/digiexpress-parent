package io.resys.thena.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
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
    CreateOneDoc docType(String docType);                 // user given classifier
    CreateOneDoc docId(@Nullable String docId);           // when empty generated by system
    CreateOneDoc parentDocId(@Nullable String parentDocId); 
    CreateOneDoc externalId(@Nullable String externalId); // user given unique id
    CreateOneDoc ownerId(@Nullable String ownerId);       //user given 'grouping' identifier for claiming ownership  
    CreateOneDoc meta(@Nullable JsonObject docMeta);

    CreateOneDoc commitAuthor(String author);
    CreateOneDoc commitMessage(String message);
    
    CreateOneDoc branch(String branchName, JsonObject branchContent); // first branch of the document, when empty generated as 'main' by the system
    CreateOneDoc commands(JsonArray commands);
    
    Uni<OneDocEnvelope> build();
  }
  interface CreateManyDocs { 
    CreateManyDocs commitAuthor(String author);
    CreateManyDocs commitMessage(String message);
    
    AddItemToCreateDoc item();
    Uni<ManyDocsEnvelope> build();
  }
  interface AddItemToCreateDoc {
    AddItemToCreateDoc docType(String docType);                 // user given classifier
    AddItemToCreateDoc parentDocId(@Nullable String parentDocId); 
    AddItemToCreateDoc docId(@Nullable String docId); 
    AddItemToCreateDoc meta(@Nullable JsonObject docMeta);
    AddItemToCreateDoc externalId(@Nullable String externalId);
    AddItemToCreateDoc ownerId(@Nullable String ownerId);
    AddItemToCreateDoc branch(String branchName, JsonObject branchContent); // first branch of the document, when empty generated as 'main' by the system
    AddItemToCreateDoc commands(JsonArray commands);
    
    CreateManyDocs next();
  }

  /*
   * Modify/Delete existing doc by: One/Many 
   */
  interface ModifyOneDoc {
    ModifyOneDoc docId(String docIdOrExternalId);
    ModifyOneDoc commitAuthor(String author);
    ModifyOneDoc commitMessage(String message);
    
    ModifyOneDoc parentDocId(@Nullable String parentDocId); 
    ModifyOneDoc externalId(@Nullable String externalId); // user given unique id
    ModifyOneDoc ownerId(@Nullable String ownerId);       //user given 'grouping' identifier for claiming ownership  
    ModifyOneDoc meta(@Nullable JsonObject docMeta);
    
    ModifyOneDoc remove();   // remove the whole document                         
    Uni<OneDocEnvelope> build();
  }
  interface ModifyManyDocs {
    ModifyManyDocs commitAuthor(String author);
    ModifyManyDocs commitMessage(String message);
    
    AddItemToModifyDoc item();
    Uni<ManyDocsEnvelope> build();
  }
  interface AddItemToModifyDoc {
    AddItemToModifyDoc docId(String docIdOrExternalId);
    AddItemToModifyDoc parentDocId(@Nullable String parentDocId); 
    AddItemToModifyDoc externalId(@Nullable String externalId); // user given unique id
    AddItemToModifyDoc ownerId(@Nullable String ownerId);       //user given 'grouping' identifier for claiming ownership  
    AddItemToModifyDoc meta(@Nullable JsonObject docMeta);
    AddItemToModifyDoc remove();
    ModifyManyDocs next();
  }
  
  
  /*
   * Modify/Delete existing branch by: One/Many 
   */
  interface CreateOneDocBranch {
    CreateOneDocBranch docId(String docId);
    CreateOneDocBranch branchFrom(@Nullable String branchIdFromWhatToCreateABranch);  // branch name from what to create the branch
    CreateOneDocBranch commitAuthor(String author);
    CreateOneDocBranch commitMessage(String message);
    
    CreateOneDocBranch commands(JsonArray commands);
    CreateOneDocBranch branch(String branchName, JsonObject branchContent);
    
    Uni<OneDocEnvelope> build();
  }
  
  interface ModifyOneDocBranch {
    ModifyOneDocBranch docId(String docId);
    ModifyOneDocBranch commit(String versionToModify);
    ModifyOneDocBranch parentIsLatest();
    ModifyOneDocBranch commitAuthor(String author);
    ModifyOneDocBranch commitMessage(String message);
    
    ModifyOneDocBranch branchName(String branchName);
    ModifyOneDocBranch commands(JsonArray commands);
    ModifyOneDocBranch replace(JsonObject newContent); 
    ModifyOneDocBranch merge(JsonObjectMerge doc);

    ModifyOneDocBranch remove(); // deletes the branch
    
    Uni<OneDocEnvelope> build();
  }
  
  interface ModifyManyDocBranches {
    int getItemsAdded();
    ModifyManyDocBranches commitAuthor(String author);
    ModifyManyDocBranches commitMessage(String message);
    
    AddItemToModifyDocBranch item();
    Uni<ManyDocsEnvelope> build();
  }
  
  interface AddItemToModifyDocBranch {
    AddItemToModifyDocBranch docId(String docId);
    AddItemToModifyDocBranch commit(String versionToModify);
    AddItemToModifyDocBranch parentIsLatest();
    AddItemToModifyDocBranch branchName(String branchName);
    AddItemToModifyDocBranch commands(JsonArray commands);
    AddItemToModifyDocBranch replace(JsonObject newContent); 
    AddItemToModifyDocBranch merge(JsonObjectMerge doc);
    AddItemToModifyDocBranch remove(); // deletes the branch
    ModifyManyDocBranches next();
  }
  
  
  @Value.Immutable
  interface OneDocEnvelope extends ThenaEnvelope {
    String getRepoId();
    @Nullable Doc getDoc();
    @Nullable DocBranch getBranch();
    @Nullable DocCommit getCommit();
    List<DocCommands> getCommands();
    List<DocCommit> getCommits();
    List<DocCommitTree> getCommitTree();
    
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
  @Value.Immutable
  interface ManyDocsEnvelope extends ThenaEnvelope {
    String getRepoId();
    List<Doc> getDoc();
    List<DocBranch> getBranch();
    List<DocCommands> getCommands();
    List<DocCommit> getCommits();
    List<DocCommitTree> getCommitTree();
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
}
