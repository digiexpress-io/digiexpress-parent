package io.resys.hdes.runtime.spi.tools;

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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceName;
import io.resys.hdes.executor.api.HdesRunnable;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.TraceBody;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeEnvir;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeTask;
import io.resys.hdes.runtime.api.ImmutableRuntimeTask;

public class ImmutableRuntimeEnvir implements RuntimeEnvir {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableRuntimeEnvir.class);
  private final HdesClassLoader classLoader;
  private final Map<String, ResourceName> executables;
  private final Map<String, Resource> values;
  private final List<Diagnostic<?>> diagnostics;
  
  @Override
  public List<Diagnostic<?>> getDiagnostics() {
    return diagnostics;
  }

  public ImmutableRuntimeEnvir(HdesClassLoader classLoader, Map<String, ResourceName> executables, Map<String, Resource> values, List<Diagnostic<?>> diagnostics) {
    super();
    this.classLoader = classLoader;
    this.executables = executables;
    this.values = values;
    this.diagnostics = diagnostics;
  }

  @Override
  public RuntimeTask get(String name) throws ClassNotFoundException {
    String lookup = name;
    if(executables.containsKey(name)) {
      ResourceName typeName = executables.get(name);
      lookup = typeName.getPkg() + "." + typeName.getName();
    }
    Class<?> clazz = classLoader.findClass(lookup);
    try {
      Resource resource = values.get(name);
      
      Class<? extends TraceBody.Accepts> accepts = (Class<? extends TraceBody.Accepts>) classLoader.findClass(resource.getAccepts().getPkg() + "$" + resource.getAccepts().getName());
      Class<? extends Trace.TraceEnd> ends = (Class<? extends Trace.TraceEnd>) classLoader.findClass(resource.getAccepts().getPkg() + "$" + resource.getEnds().getName());
      Class<? extends TraceBody.Returns> returns = (Class<? extends TraceBody.Returns>) classLoader.findClass(resource.getAccepts().getPkg() + "$" + resource.getReturns().getName());
      HdesRunnable executable = (HdesRunnable) clazz.getConstructors()[0].newInstance();
      
      return ImmutableRuntimeTask.builder()
          .name(name)
          .accepts(accepts)
          .returns(returns)
          .ends(ends)
          .value(executable)
          .build();
    } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static ImmutableRuntimeEnvir from(
      HdesJavaFileManager fileManager, 
      List<Diagnostic<?>> diagnostics, 
      Map<String, ResourceName> executables, 
      Map<String, Resource> values) {
    
    List<Diagnostic<?>> errors = diagnostics.stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
    if (!errors.isEmpty()) {
      LOGGER.error(errors.toString());
      System.err.println(errors);
    }
    
    Map<String, HdesJavaFileObject> cache = fileManager.getCache();
    HdesClassLoader classLoader = new HdesClassLoader(cache);
    return new ImmutableRuntimeEnvir(classLoader, executables, values, diagnostics);
  }

  private static class HdesClassLoader extends ClassLoader {
    private final Map<String, HdesJavaFileObject> cache;

    public HdesClassLoader(Map<String, HdesJavaFileObject> cache) {
      super(HdesClassLoader.class.getClassLoader());
      this.cache = cache;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      try {
        HdesJavaFileObject javaFileObject = cache.get(name);
        
        if (javaFileObject == null) {
          return super.findClass(name);
        }
        byte[] bytes = javaFileObject.getBytes();
        return defineClass(name, bytes, 0, bytes.length);
      } catch (ClassNotFoundException e) {
        StringBuilder msg = new StringBuilder()
            .append("Failed to find class with name: ").append(name).append("!").append(System.lineSeparator())
            .append("Known generated class names are: ");
        
        for(String gen : cache.keySet()) {
          msg.append("  - ").append(gen).append(System.lineSeparator());
        }
        msg.append("Original exception message: ").append(e.getMessage());
        throw new ClassNotFoundException(msg.toString(), e);
      }
    }
  }
}
