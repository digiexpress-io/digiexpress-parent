package io.resys.limaone.ast.attribute;

import java.util.Map;

public interface AST {
  String getId();
  String getName();
  
  Map<String, Attribute_AST> getInputs();
  Map<String, Attribute_AST> getOutputs();
}