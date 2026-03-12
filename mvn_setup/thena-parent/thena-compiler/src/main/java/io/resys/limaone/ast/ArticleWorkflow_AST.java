package io.resys.limaone.ast;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;


@Value.Immutable
@JsonSerialize(as = ImmutableArticleWorkflow_AST.class)
@JsonDeserialize(as = ImmutableArticleWorkflow_AST.class)
public interface ArticleWorkflow_AST extends Simple_AST {
  List<Dependency_AST> getDependencies();
}
