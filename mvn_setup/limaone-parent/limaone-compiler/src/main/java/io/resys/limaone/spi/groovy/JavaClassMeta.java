package io.resys.limaone.spi.groovy;

import java.lang.reflect.Modifier;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ClassNode;

import lombok.Data;

@Data
public class JavaClassMeta {
  private final String packageName;
  private final List<String> imports;
  private final String className;
  private final String body;
  
  
  public static org.codehaus.groovy.ast.MethodNode findExecuteMethod(ClassNode classNode) {
    for(org.codehaus.groovy.ast.MethodNode method : classNode.getMethods()) {
      final var isMods = Modifier.isPublic(method.getModifiers()) && !Modifier.isVolatile(method.getModifiers());
      final var isName = method.getName().equals("execute");
      if(!isMods || !isName) {
        continue;
      }
      if(method.getParameters().length > 2) {
        continue;
      }
      
      if(method.getReturnType() == null) {
        continue;
      }
      
      return method;
    }
    
    return null;
  }
  
  public static JavaClassMeta parse(String javaSyntax) {
    String packageName = null;
    List<String> imports = new ArrayList<>();
    String className = null;
    
    // Package pattern: package some.package.name;
    Pattern packagePattern = Pattern.compile("^\\s*package\\s+([\\w\\.]+)\\s*;");
    
    // Import pattern: import some.package.Class;
    Pattern importPattern = Pattern.compile("^\\s*import\\s+([\\w\\.\\*]+)\\s*;");
    
    // Class pattern: public class ClassName
    Pattern classPattern = Pattern.compile("^\\s*public\\s+class\\s+(\\w+)");
    
    String[] lines = javaSyntax.split("\\r?\\n");
    StringBuilder bodyBuilder = new StringBuilder();
    boolean inClassBody = false;
    int braceCount = 0;
    
    for (String line : lines) {
      Matcher packageMatcher = packagePattern.matcher(line);
      if (packageMatcher.find()) {
        packageName = packageMatcher.group(1);
        continue;
      }
      
      Matcher importMatcher = importPattern.matcher(line);
      if (importMatcher.find()) {
        imports.add(importMatcher.group(1));
        continue;
      }
      
      Matcher classMatcher = classPattern.matcher(line);
      if (classMatcher.find()) {
        className = classMatcher.group(1);
        inClassBody = true;
        
        // Count opening braces in class declaration line
        for (char c : line.toCharArray()) {
          if (c == '{') braceCount++;
          else if (c == '}') braceCount--;
        }
        continue;
      }
      
      if (inClassBody) {
        bodyBuilder.append(line).append("\n");
        
        // Count braces to find end of class
        for (char c : line.toCharArray()) {
          if (c == '{') braceCount++;
          else if (c == '}') braceCount--;
        }
        
        // If braces balance to 0, we've reached end of class
        if (braceCount == 0) {
          break;
        }
      }
    }
    
    String body = bodyBuilder.toString().trim();
    
    return new JavaClassMeta(packageName, imports, className, body);
  }
  
  public static String makePackageSafe(String id) {
    if (id == null || id.isEmpty()) {
      return "unknown";
    }
    
    return id.replaceAll("[^a-zA-Z0-9_]", "_")
             .replaceAll("^[0-9]", "_$0")
             .toLowerCase();
  }
}
