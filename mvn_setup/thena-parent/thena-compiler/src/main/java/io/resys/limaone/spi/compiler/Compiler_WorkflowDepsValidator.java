package io.resys.limaone.spi.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.compiler.CompilableUnit.Artifact;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_WorkflowDepsValidator {
  private final Artifact artifact;
  private final ArticleWorkflow_AST ast;
  private final Map<String, Dependency_AST> childrenByName = new HashMap<>();
  private final List<ModelError> errors = new ArrayList<>();
  


  /**
   * Walk the entire Flow AST starting from the root statement
   */
  public List<ModelError> walk() {
    artifact.getChildDeps().forEach(dep -> childrenByName.put(dep.getDependencyId(), dep));
    
    
    return errors;
  }


}
