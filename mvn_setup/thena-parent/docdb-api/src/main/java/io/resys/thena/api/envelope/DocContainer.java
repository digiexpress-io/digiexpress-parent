package io.resys.thena.api.envelope;

import java.util.List;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;

public interface DocContainer extends ThenaContainer {
  
  interface DocObjectsVisitor<T> {
    T visit(Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log);
  }
}
