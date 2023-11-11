package io.resys.thena.docdb.models.doc.actions;

import io.resys.thena.docdb.api.actions.DocCommitActions;
import io.resys.thena.docdb.models.doc.commitmany.CreateManyDocsImpl;
import io.resys.thena.docdb.models.doc.commitmany.ModifyManyDocBranchesImpl;
import io.resys.thena.docdb.models.doc.commitmany.ModifyManyDocsImpl;
import io.resys.thena.docdb.models.doc.commitone.CreateOneDocBranchImpl;
import io.resys.thena.docdb.models.doc.commitone.CreateOneDocImpl;
import io.resys.thena.docdb.models.doc.commitone.ModifyOneDocBranchImpl;
import io.resys.thena.docdb.models.doc.commitone.ModifyOneDocImpl;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocAppendActionsImpl implements DocCommitActions {
  private final DbState state;

  @Override
  public CreateOneDoc createOneDoc() {
    return new CreateOneDocImpl(state);
  }
  @Override
  public ModifyOneDoc modifyOneDoc() {
    return new ModifyOneDocImpl(state);
  }
  @Override
  public CreateOneDocBranch branchOneDoc() {
    return new CreateOneDocBranchImpl(state);
  }
  @Override
  public ModifyOneDocBranch modifyOneBranch() {
    return new ModifyOneDocBranchImpl(state);
  }
  @Override
  public CreateManyDocs createManyDocs() {
    return new CreateManyDocsImpl(state);
  }
  @Override
  public ModifyManyDocBranches modifyManyBranches() {
    return new ModifyManyDocBranchesImpl(state);
  }
  @Override
  public ModifyManyDocs modifyManyDocs() {
    return new ModifyManyDocsImpl(state);
  }
}
