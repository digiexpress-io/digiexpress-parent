package io.resys.limaone.spi.groovy;

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
import java.util.List;
import java.util.regex.Pattern;

import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroovyErrorParser {

  @Data
  private static class GroovyError {
    private final String fileName;
    private final int lineNumber;
    private final int columnNumber;
    private final String message;
    private final String codeSnippet;
  }

  //Matches "FileName.groovy: 1: message"
  private static final Pattern HEADER_PATTERN = Pattern.compile("^([^:]+\\.groovy):\\s*(\\d+):\\s*(.*)$");
  // Matches " @ line 1, column 391."
  private static final Pattern METADATA_PATTERN = Pattern.compile("^\\s*@ line\\s*(\\d+),\\s*column\\s*(\\d+)\\.");
  // Matches the visual indicator "   ^"
  private static final Pattern MARKER_PATTERN = Pattern.compile("^\\s*\\^\\s*$");

  public static List<ModelError> parseErrors(String errorOutput, String origin) {
    final var errors = new ArrayList<ModelError>();
    final var lines = errorOutput.split("\\r?\\n");

    for (int i = 0; i < lines.length; i++) {
      final var line = lines[i].trim();
      final var headerMatcher = HEADER_PATTERN.matcher(line);

      if (headerMatcher.matches()) {
        final var fileName = headerMatcher.group(1);
        final int lineNumber = Integer.parseInt(headerMatcher.group(2));
        final var message = headerMatcher.group(3);
        
        int columnNumber = 0;
        String codeSnippet = null;

        // 1. Look for the Metadata line (@ line...)
        if (i + 1 < lines.length) {
          final var metaMatcher = METADATA_PATTERN.matcher(lines[i + 1]);
          if (metaMatcher.find()) {
            columnNumber = Integer.parseInt(metaMatcher.group(2));
            
            // 2. The code snippet is usually the VERY NEXT line
            if (i + 2 < lines.length) {
                codeSnippet = lines[i + 2].trim();
                
                // 3. The marker (^) is the line after that
                if (i + 3 < lines.length && MARKER_PATTERN.matcher(lines[i+3]).matches()) {
                    // Optimization: If marker exists, we trust its visual position
                    // columnNumber = lines[i + 3].indexOf('^') + 1; 
                }
            }
            // Skip the lines we just consumed
            i += 2; 
          }
        }

        errors.add(createMessage(new GroovyError(fileName, lineNumber, columnNumber, message, codeSnippet), origin));
      }
    }
    
    if(errors.isEmpty()) {
      errors.add(createMessage(new GroovyError("", 1, 1, errorOutput, ""), origin));
    }
    
    return errors;
  }
  
  private static ModelError createMessage(GroovyError error, String origin) {
    int actualLine = 0;
    final String snippet = error.getCodeSnippet();

    if (snippet != null && !snippet.isEmpty()) {
      String[] sourceLines = origin.split("\\r?\\n");
      for (int i = 0; i < sourceLines.length; i++) {
        // We trim to handle indentation differences between the compiler output and src
        if (sourceLines[i].contains(snippet)) {
            actualLine = i + 1; // +1 because array is 0-indexed, editors are 1-indexed
            break;
        }
      }
    }

    // Fallback if snippet search fails
    if (actualLine == 0) {
      actualLine = error.getLineNumber(); 
    }

    return ImmutableModelError.builder()
      .line(actualLine)
      .msg(error.getMessage() + System.lineSeparator() + "        broken at > " + snippet)
      .build();
  }

}
