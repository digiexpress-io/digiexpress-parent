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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class HdesJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
  private final Map<String, HdesJavaFileObject> cache = new HashMap<>();

  public HdesJavaFileManager(StandardJavaFileManager standardManager) {
    super(standardManager);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(
      JavaFileManager.Location location,
      String className,
      JavaFileObject.Kind kind,
      FileObject sibling) {
    HdesJavaFileObject result = HdesJavaFileObject.create(className, kind);
    cache.put(className, result);
    return result;
  }
  
  public Map<String, HdesJavaFileObject> getCache() {
    return Collections.unmodifiableMap(cache);
  }
  
  public static HdesJavaFileManager create(JavaCompiler compiler) {
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    return new HdesJavaFileManager(fileManager);
  }
}
