package io.resys.hdes.compiler.api;

import io.resys.hdes.ast.api.nodes.HdesNode;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class HdesCompilerException extends RuntimeException {

  private static final long serialVersionUID = -7831610317362075176L;

  public HdesCompilerException() {
    super();
  }

  public HdesCompilerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public HdesCompilerException(String message, Throwable cause) {
    super(message, cause);
  }

  public HdesCompilerException(String message) {
    super(message);
  }

  public HdesCompilerException(Throwable cause) {
    super(cause);
  }
  

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public String unknownExpression(HdesNode ast) {
      return new StringBuilder()
          .append("Unknown expression AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }

    public String unknownHeader(HdesNode ast) {
      return new StringBuilder()
          .append("Unknown header AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }

    public String unknownFunctionCall(HdesNode ast, String was) {
      return new StringBuilder()
          .append("Unknown function in expression!").append(System.lineSeparator())
          .append("Function: ").append(was).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
  }
}
