package io.resys.thena.docdb.spi.doc;

import io.resys.thena.docdb.api.actions.DocCommitActions;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.doc.commits.AppendDocBranchImpl;
import io.resys.thena.docdb.spi.doc.commits.AppendDocImpl;
import io.resys.thena.docdb.spi.doc.commits.CreateDocBranchImpl;
import io.resys.thena.docdb.spi.doc.commits.CreateDocImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocAppendActionsImpl implements DocCommitActions {
  private final DbState state;

  @Override
  public CreateDoc createDoc() {
    return new CreateDocImpl(state);
  }
  @Override
  public AppendDoc appendDoc() {
    return new AppendDocImpl(state);
  }
  @Override
  public CreateDocBranch branchDoc() {
    return new CreateDocBranchImpl(state);
  }
  @Override
  public AppendDocBranch appendBranch() {
    return new AppendDocBranchImpl(state);
  }
}
