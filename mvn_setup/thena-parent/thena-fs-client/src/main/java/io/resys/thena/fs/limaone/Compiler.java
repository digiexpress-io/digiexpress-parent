package io.resys.thena.fs.limaone;

import io.resys.thena.fs.entities.Tree;
import io.smallrye.mutiny.Multi;

public interface Compiler {
  Multi<Artifact> compile(Tree tree);
}
