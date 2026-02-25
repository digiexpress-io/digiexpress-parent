package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableHeaders_AST.class)
@JsonDeserialize(as = ImmutableHeaders_AST.class)
@Value.Immutable
public
interface Headers_AST extends Serializable {
  List<Attribute_AST> getAcceptDefs();
  List<Attribute_AST> getReturnDefs();
}