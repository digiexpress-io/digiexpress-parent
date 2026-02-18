package io.resys.thena.fs.spi.committree;


interface CommitTreeVisitor {
  void visit(CommitTree previous, CommitTree next);
}