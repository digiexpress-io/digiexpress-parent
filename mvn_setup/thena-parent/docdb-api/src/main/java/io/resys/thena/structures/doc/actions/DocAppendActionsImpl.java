package io.resys.thena.structures.doc.actions;

import io.resys.thena.api.actions.DocCommitActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.doc.commitmany.CreateManyDocsImpl;
import io.resys.thena.structures.doc.commitmany.ModifyManyDocBranchesImpl;
import io.resys.thena.structures.doc.commitmany.ModifyManyDocsImpl;
import io.resys.thena.structures.doc.commitone.CreateOneDocBranchImpl;
import io.resys.thena.structures.doc.commitone.CreateOneDocImpl;
import io.resys.thena.structures.doc.commitone.ModifyOneDocBranchImpl;
import io.resys.thena.structures.doc.commitone.ModifyOneDocImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocAppendActionsImpl implements DocCommitActions {
  private final DbState state;
  private final String repoId;

  @Override
  public CreateOneDoc createOneDoc() {
    return new CreateOneDocImpl(state, repoId);
  }
  @Override
  public ModifyOneDoc modifyOneDoc() {
    return new ModifyOneDocImpl(state, repoId);
  }
  @Override
  public CreateOneDocBranch branchOneDoc() {
    return new CreateOneDocBranchImpl(state, repoId);
  }
  @Override
  public ModifyOneDocBranch modifyOneBranch() {
    return new ModifyOneDocBranchImpl(state, repoId);
  }
  @Override
  public CreateManyDocs createManyDocs() {
    return new CreateManyDocsImpl(state, repoId);
  }
  @Override
  public ModifyManyDocBranches modifyManyBranches() {
    return new ModifyManyDocBranchesImpl(state, repoId);
  }
  @Override
  public ModifyManyDocs modifyManyDocs() {
    return new ModifyManyDocsImpl(state, repoId);
  }
}
