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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import groovy.lang.GroovyClassLoader;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.groovy.FailSafeService;
import io.resys.limaone.spi.groovy.GroovyErrorParser;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FormStepCompiler {

  private static final Pattern FINAL_DECL_PATTERN = Pattern.compile(
      "^\\s*final\\s+([\\w<>\\[\\],.\\s]+?)\\s+(\\w+)\\s*=");

  @Builder @Getter
  public static class FormStepResult {
    private final Class<?> compiledClass;
    private final Map<String, String> outputFields;
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
      final var parsed = GroovyErrorParser.parseErrors(e.getMessage(), returnsCode);
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

  static Map<String, String> extractFinalDeclarations(String returnsCode) {
    final Map<String, String> fields = new LinkedHashMap<>();
    for (final var line : returnsCode.split("\\r?\\n")) {
      final Matcher matcher = FINAL_DECL_PATTERN.matcher(line);
      if (matcher.find()) {
        final var type = matcher.group(1);
        final var name = matcher.group(2);
        fields.put(name, type);
      }
    }
    return fields;
  }

  static String generateSource(String taskId, String returnsCode, Map<String, String> outputFields) {
    final var safeName = taskId.replaceAll("[^a-zA-Z0-9_]", "_");
    final var src = new StringBuilder();
    
    src.append("package io.resys.limaone.spi.compiler.groovy.form;").append(System.lineSeparator());
    src.append(System.lineSeparator());
    src.append("public class FormStep_").append(safeName).append(" {").append(System.lineSeparator());
    src.append(System.lineSeparator());

    src.append("  public Output execute(FormInstance form) {").append(System.lineSeparator());
    for (final var line : returnsCode.split("\\r?\\n")) {
      src.append("    ").append(line).append(System.lineSeparator());
    }
    src.append(System.lineSeparator());
    src.append("    Output output = new Output();").append(System.lineSeparator());
    for (final var field : outputFields.keySet()) {
      src.append("    output.").append(field).append(" = ").append(field).append(";").append(System.lineSeparator());
    }
    src.append("    return output;").append(System.lineSeparator());
    src.append("  }").append(System.lineSeparator());
    src.append(System.lineSeparator());

    src.append("  public static class Output {").append(System.lineSeparator());
    for (final var entry : outputFields.entrySet()) {
      src.append("    ").append(entry.getValue()).append(" ").append(entry.getKey()).append(";").append(System.lineSeparator());
    }
    src.append("  }").append(System.lineSeparator());

    src.append("}").append(System.lineSeparator());
    return src.toString();
  }
}
