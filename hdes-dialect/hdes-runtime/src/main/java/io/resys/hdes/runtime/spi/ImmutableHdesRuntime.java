package io.resys.hdes.runtime.spi;

/*-
 * #%L
 * hdes-runtime
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceDeclaration;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceName;
import io.resys.hdes.runtime.api.HdesRuntime;
import io.resys.hdes.runtime.spi.tools.HdesJavaFileManager;
import io.resys.hdes.runtime.spi.tools.HdesJavaFileObject;
import io.resys.hdes.runtime.spi.tools.ImmutableRuntimeEnvir;

public class ImmutableHdesRuntime implements HdesRuntime {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableHdesRuntime.class);
  
  public static Builder builder() {
    return new ImmutableEnvirBuilder();
  }
  
  public static class ImmutableEnvirBuilder implements Builder {

    private final List<Resource> resources = new ArrayList<>();

    @Override
    public RuntimeEnvir build() {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      HdesJavaFileManager fileManager = HdesJavaFileManager.create(compiler);
      
      DiagnosticCollector<Object> diagnosticListener = new DiagnosticCollector<Object>();
      List<String> options = Arrays.asList();
      StringWriter out = new StringWriter();
      List<String> annotatedClasses = new ArrayList<>();
      List<SimpleJavaFileObject> files = new ArrayList<>();
      Map<String, ResourceName> executables = new HashMap<>();
      
      Map<String, Resource> values = new HashMap<>();
      
      for(Resource resource : resources) {
        values.put(resource.getName(), resource);
        
        // Type names for annotation processor
        for(ResourceName typeName : resource.getTypes()) {
          annotatedClasses.add(typeName.getPkg() + "." + typeName.getName());
        }
        
        // Java source code
        for(ResourceDeclaration typeDeclaration : resource.getDeclarations()) {
          log(typeDeclaration);
          files.add(HdesJavaFileObject.create(typeDeclaration.getType().getName(), typeDeclaration.getValue()));
          if(typeDeclaration.isExecutable()) {
            executables.put(resource.getName(), typeDeclaration.getType());
          }
        }
      }
      
      CompilationTask task = compiler.getTask(out, fileManager, diagnosticListener, options, annotatedClasses, files);
      var immutables = new org.immutables.processor.ProxyProcessor(); 
      task.setProcessors(Arrays.asList(immutables));
      task.call();
      
      List<Diagnostic<?>> diagnostics = diagnosticListener.getDiagnostics();
      return ImmutableRuntimeEnvir.from(fileManager, diagnostics, executables, Collections.unmodifiableMap(values));
    }

    @Override
    public Builder from(List<Resource> resources) {
      if(resources != null) {
        this.resources.addAll(resources);
      }
      return this;
    }
  }
  
  private static void log(ResourceDeclaration type) {
    if(LOGGER.isDebugEnabled()) {
      
      StringBuilder result = new StringBuilder().append(System.lineSeparator())
          .append("Declaration: '").append(type.getType().getName()).append("'")
          .append(System.lineSeparator());
      
      int index = 1;
      for(String value : type.getValue().split("\\r?\\n")) {
        result.append(index++).append(":   ").append(value).append(System.lineSeparator());
      }
      
      LOGGER.debug(result.toString());
    }
  }
}
