package io.resys.thena.docdb.spi.doc.commits;

import java.awt.List;
import java.util.ArrayList;

import io.resys.thena.docdb.api.actions.DocCommitActions.AddItemToCreateDoc;
import io.resys.thena.docdb.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.docdb.api.actions.DocCommitActions.ManyDocEnvelope;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class CreateManyDocsImpl implements CreateManyDocs {
  private String repoId;
  private String branchName;
  private String docType;
  private String author;
  private String message;
  private final List items = new ArrayList<>();
  
  @Override
  public CreateManyDocs repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public CreateManyDocs branchName(String branchName) {
    RepoAssert.isName(branchName, () -> "branchName has invalid charecters!");
    this.branchName = branchName;
    return this;
  }
  @Override
  public CreateManyDocs docType(String docType) {
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    this.docType = docType;
    return this;
  }
  @Override
  public CreateManyDocs author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public CreateManyDocs message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }

  @Override
  public AddItemToCreateDoc item() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ManyDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.isTrue(!items.isEmpty(), () -> "Nothing to commit, no items!");
        
    return null;
  }

}
