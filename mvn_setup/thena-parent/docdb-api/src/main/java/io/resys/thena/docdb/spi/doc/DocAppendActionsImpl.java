package io.resys.thena.docdb.spi.doc;

import io.resys.thena.docdb.api.actions.DocCommitActions;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.doc.commits.ModifyOneDocBranchImpl;
import io.resys.thena.docdb.spi.doc.commits.ModifyOneDocImpl;
import io.resys.thena.docdb.spi.doc.commits.CreateManyDocsImpl;
import io.resys.thena.docdb.spi.doc.commits.CreateOneDocBranchImpl;
import io.resys.thena.docdb.spi.doc.commits.CreateOneDocImpl;
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
  public CreateManyDocs createManyDoc() {
    return new CreateManyDocsImpl(state);
  }
  @Override
  public ModifyManyDocs modifyManyDocs() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ModifyManyDocBranches modifyManyBranches() {
    // TODO Auto-generated method stub
    return null;
  }
}
