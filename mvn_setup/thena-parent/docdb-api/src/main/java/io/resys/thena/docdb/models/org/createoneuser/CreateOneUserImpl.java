package io.resys.thena.docdb.models.org.createoneuser;

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.actions.ImmutableOneUserEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.CreateOneUser;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneUserEnvelope;
import io.resys.thena.docdb.models.doc.DocInserts.DocBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneUserImpl implements CreateOneUser {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String userName;
  private String email;
  private String externalId;

  private List<String> addUserToGroups = new ArrayList<>();
  private List<String> addUserToRoles = new ArrayList<>();

  
  @Override public CreateOneUserImpl repoId(String repoId) {         this.repoId = RepoAssert.notEmpty(repoId,           () -> "repoId can't be empty!"); return this; }
  @Override public CreateOneUserImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneUserImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneUserImpl userName(String userName) {     this.userName = RepoAssert.notEmpty(userName,       () -> "userName can't be empty!"); return this; }
  @Override public CreateOneUserImpl email(String email) {           this.email = RepoAssert.notEmpty(email,             () -> "email can't be empty!"); return this; }
  
  @Override public CreateOneUserImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneUserImpl addUserToGroups(List<String> addUserToGroups) { this.addUserToGroups.addAll(RepoAssert.notNull(addUserToGroups, () -> "addUserToGroups can't be empty!")); return this; }
  @Override public CreateOneUserImpl addUserToRoles(List<String> addUserToRoles) { this.addUserToRoles.addAll(RepoAssert.notNull(addUserToRoles, () -> "addUserToRoles can't be empty!")); return this; }
  
  @Override
  public Uni<OneUserEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(userName, () -> "userName can't be empty!");
    RepoAssert.notEmpty(email, () -> "email can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<OneUserEnvelope> doInTx(OrgRepo tx) {
    final DocBatchForOne batch = null; /*new BatchForOneDocCreate(tx.getRepo().getId(), docType, author, message, branchName)
        .docId(docId)
        .ownerId(ownerId)
        .externalId(externalId)
        .parentDocId(parentDocId)
        .log(appendLogs)
        .meta(appendMeta)
        .append(appendBlobs)
        .create(); */

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> ImmutableOneUserEnvelope.builder()
        .repoId(repoId)
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build());
  }

}
