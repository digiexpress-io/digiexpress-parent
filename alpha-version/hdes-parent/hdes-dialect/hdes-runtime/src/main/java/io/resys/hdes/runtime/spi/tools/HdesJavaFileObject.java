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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

public abstract class HdesJavaFileObject extends SimpleJavaFileObject {
  
  private String className;
  
  public HdesJavaFileObject(String className, URI uri, JavaFileObject.Kind kind) {
    super(uri, kind);
    this.className = className;
  }
  
  public String getClassName() {
    return className;
  }
  
  public abstract byte[] getBytes();

  public static HdesJavaFileObject create(String className, CharSequence content) {
    final URI uri = URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension);
    final JavaFileObject.Kind kind = JavaFileObject.Kind.SOURCE;
    return new HdesJavaFileObject(className, uri, kind) {
      @Override
      public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
      }
      @Override
      public byte[] getBytes() {
        return content.toString().getBytes(StandardCharsets.UTF_8);
      }
    };
  }

  public static HdesJavaFileObject create(String className, JavaFileObject.Kind kind) {
    final URI uri = URI.create("string:///" + className.replace('.', '/') + kind.extension);
    return new HdesJavaFileObject(className, uri, kind) {
      private ByteArrayOutputStream content = new ByteArrayOutputStream();
      private byte[] bytes;

      @Override
      public OutputStream openOutputStream() {
        return content;
      }

      @Override
      public byte[] getBytes() {
        if (bytes == null) {
          bytes = content.toByteArray();
          content = null;
        }
        return bytes;
      }

      @Override
      public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return new String(content.toByteArray(), StandardCharsets.UTF_8);
      }
    };
  }
}
