package io.resys.limaone.spi.ast;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import groovy.lang.GroovyClassLoader;
import io.resys.limaone.ast.Flow_AST.FormOutputField;
import io.resys.limaone.spi.ast.FlowStatementFactory.ImmutableFormOutputField;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.groovy.FailSafeService;
import io.resys.limaone.spi.groovy.GroovyErrorParser;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FormStepCompiler {

  private static final Pattern FINAL_DECL_PATTERN = Pattern.compile("^\\s*final\\s+([\\w<>\\[\\],.\\s]+?)\\s+(\\w+)\\s*=");
  private static final String SOURCE_TEMPLATE = """
package io.resys.limaone.spi.compiler.groovy.form;

public class FormStep_@@SAFE_NAME@@ {

  public Output execute(FormInstance form) {
@@EXECUTE_BODY@@
    Output output = new Output();
@@OUTPUT_ASSIGNMENTS@@
    return output;
  }

  public static class Output {
@@OUTPUT_FIELD_DECLS@@
  }
}
""";

  @Builder @Getter
  public static class FormStepResult {
    private final Class<?> compiledClass;
    private final List<FormOutputField> outputFields;
    @Builder.Default
    private final List<ModelError> errors = Collections.emptyList();
  }

  public static FormStepResult compile(GroovyClassLoader gcl, String taskId, String returnsCode, int returnsStartLine) {
    final var outputFields = extractFinalDeclarations(returnsCode);
    final var source = generateSource(taskId, returnsCode, outputFields);
    
    try {
      final Class<?> compiled = gcl.parseClass(source);
      return FormStepResult.builder()
          .compiledClass(compiled)
          .outputFields(outputFields)
          .build();
    } catch (Exception e) {
      final var msg = "Failed to compile form step '" + taskId + "': " + e.getMessage() +
          System.lineSeparator() + source;
      log.error(msg, e);
      final var parsed = GroovyErrorParser.parseErrors(e.getMessage(), returnsCode != null ? returnsCode : "");
      final List<ModelError> errors = parsed.stream()
          .map(error -> (ModelError) ImmutableModelError.builder()
              .id(error.getId())
              .msg(error.getMsg())
              .column(error.getColumn())
              .exception(error.getException())
              .line(error.getLine() != null ? error.getLine() + returnsStartLine : returnsStartLine)
              .build())
          .collect(Collectors.toList());
      return FormStepResult.builder()
          .compiledClass(FailSafeService.class)
          .outputFields(outputFields)
          .errors(errors)
          .build();
    }
  }

  static List<FormOutputField> extractFinalDeclarations(String returnsCode) {
    if (returnsCode == null || returnsCode.isEmpty()) {
      return List.of();
    }
    final List<FormOutputField> fields = new ArrayList<>();
    for (final var line : returnsCode.split("\\r?\\n")) {
      final Matcher matcher = FINAL_DECL_PATTERN.matcher(line);
      if (matcher.find()) {
        final var declaredType = matcher.group(1).trim();
        final var name = matcher.group(2);
        fields.add(new ImmutableFormOutputField(name, declaredType));
      }
    }
    return Collections.unmodifiableList(fields);
  }

  static String generateSource(String taskId, String returnsCode, List<FormOutputField> outputFields) {
    final var safeName = taskId.replaceAll("[^a-zA-Z0-9_]", "_");
    return SOURCE_TEMPLATE
        .replace("@@SAFE_NAME@@", safeName)
        .replace("@@EXECUTE_BODY@@", formatExecuteBody(returnsCode))
        .replace("@@OUTPUT_ASSIGNMENTS@@", formatOutputAssignments(outputFields))
        .replace("@@OUTPUT_FIELD_DECLS@@", formatOutputFieldDeclarations(outputFields));
  }

  private static String formatExecuteBody(String returnsCode) {
    if (returnsCode == null || returnsCode.trim().isEmpty()) {
      return "";
    }
    final var lines = returnsCode.split("\\r?\\n", -1);
    final var executeIndent = "    ";
    final var sb = new StringBuilder();
    for (final var line : lines) {
      sb.append(executeIndent).append(line.stripLeading()).append(System.lineSeparator());
    }
    return sb.toString();
  }

  private static String formatOutputAssignments(List<FormOutputField> outputFields) {
    if (outputFields.isEmpty()) {
      return "";
    }
    final var sb = new StringBuilder();
    for (final var field : outputFields) {
      sb.append("    output.")
          .append(field.getName())
          .append(" = ")
          .append(field.getName())
          .append(";")
          .append(System.lineSeparator());
    }
    return sb.toString();
  }

  private static String formatOutputFieldDeclarations(List<FormOutputField> outputFields) {
    if (outputFields.isEmpty()) {
      return "";
    }
    final var sb = new StringBuilder();
    for (final var field : outputFields) {
      sb.append("    ")
          .append(field.getDeclaredType())
          .append(" ")
          .append(field.getName())
          .append(";")
          .append(System.lineSeparator());
    }
    return sb.toString();
  }
}
