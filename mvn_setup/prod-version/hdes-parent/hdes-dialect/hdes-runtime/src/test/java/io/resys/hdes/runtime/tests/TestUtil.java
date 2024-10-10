package io.resys.hdes.runtime.tests;

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

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.resys.hdes.compiler.api.HdesCompiler;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.spi.GenericHdesCompiler;
import io.resys.hdes.executor.api.HdesDef;
import io.resys.hdes.executor.api.HdesDefContinue;
import io.resys.hdes.executor.api.HdesDefContinue.HdesWakeupValue;
import io.resys.hdes.executor.api.ImmutableHdesWakeup;
import io.resys.hdes.executor.api.ImmutableHdesWakeupValue;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.TraceBody;
import io.resys.hdes.executor.spi.visitors.DebugCSVHdesTraceVisitor;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeEnvir;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeTask;
import io.resys.hdes.runtime.spi.ImmutableHdesRuntime;

public class TestUtil {
  private static final HdesCompiler compiler = GenericHdesCompiler.config().build();
  private static final ObjectMapper json = new ObjectMapper();
  private static final ObjectMapper yaml = new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module());

  public static TestBuilder runtime() {
    return new TestBuilder();
  }
  
  public static class TestBuilder {
    private final List<String> src = new ArrayList<>();
    public TestBuilder src(String src) {
      this.src.add(src);
      return this;
    }
    public TestRunner build(String target) {
      final var parser = compiler.parser();
      this.src.forEach(s -> parser.add(UUID.randomUUID().toString(), s));
      
      final List<Resource> resources = parser.build();
      final RuntimeEnvir runtime = ImmutableHdesRuntime.builder().from(resources).build();
      RuntimeTask task;
      try {
        task = runtime.get(target);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      return new TestRunner(task, runtime);
    }
  }
  
  public static class TestRunner {
    @SuppressWarnings("unused")
    private final RuntimeEnvir runtime;
    private final RuntimeTask task;
    public TestRunner(RuntimeTask task, RuntimeEnvir runtime) {
      super();
      this.runtime = runtime;
      this.task = task;
    }
    public TestRunnerAccepts accepts() {
      return new TestRunnerAccepts(task);
    }
    
    public TestRunnerWakeup wakeup() {
      return new TestRunnerWakeup(task);
    }
  }

  public static class TestRunnerWakeup {
    private final RuntimeTask task;
    private final List<HdesWakeupValue> accepts = new ArrayList<>();
    public TestRunnerWakeup(RuntimeTask task) {
      super();
      this.task = task;
    }

    public TestRunnerWakeup accepts(String dataId, Serializable data) {
      accepts.add(ImmutableHdesWakeupValue.builder().data(data).dataId(dataId).build());
      return this;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TraceEnd build(TraceEnd trace) {
      HdesDefContinue def = (HdesDefContinue) task.getValue();
      return def.apply(trace, ImmutableHdesWakeup.builder().values(accepts).build());
    }
  }
  
  public static class TestRunnerAccepts {
    private final RuntimeTask task;
    private final Map<String, Serializable> accepts = new HashMap<>();
    public TestRunnerAccepts(RuntimeTask task) {
      super();
      this.task = task;
    }

    public TestRunnerAccepts value(String name, Serializable value) {
      accepts.put(name, value);
      return this;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TraceEnd build() {
      TraceBody.Accepts input = getJson().convertValue(accepts, task.getAccepts());
      HdesDef def = (HdesDef) task.getValue();
      return def.apply(input);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static String csv(TraceEnd end) {
    return new DebugCSVHdesTraceVisitor(d -> {
      try {
        return getJson().convertValue(d, HashMap.class);
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
      
    }).visitBody(end);
  }
  
  
  public static String yaml(Object node) {
    try {
      return yaml.writeValueAsString(node);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  
  public static String file(String name) {
    try {
      return IOUtils.toString(TestUtil.class.getClassLoader().getResourceAsStream(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public static ObjectMapper getJson() {
    return json;
  }

  public static ObjectMapper getYaml() {
    return yaml;
  }

  public static HdesCompiler getCompiler() {
    return compiler;
  }
}
