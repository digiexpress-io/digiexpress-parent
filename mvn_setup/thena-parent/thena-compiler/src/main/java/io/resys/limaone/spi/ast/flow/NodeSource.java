package io.resys.limaone.spi.ast.flow;

import lombok.Value;

@Value
public class NodeSource {
  String line;
  int lineNumber;
}
