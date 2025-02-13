package io.resys.hdes.compiler.spi.st.mapping;

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

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.compiler.spi.st.visitors.StSpec;

public class ServiceMappingFactory {

  @Value.Immutable
  public interface StMappingSpec extends StSpec {
    Consumer<CodeBlock.Builder> getValue();
  }
  
  private static final ServiceAcceptsMapping accepts = new ServiceAcceptsMapping();
  private static final ServiceReturnsMapping returns = new ServiceReturnsMapping();
  
  public static CodeBlock accepts(ObjectMappingDef mapping, HdesTree ctx) {
    return accepts.visitBody(mapping, ctx);
  }
  
  public static CodeBlock returns(ServiceTree ctx) {
    return returns.visitBody(ctx);
  }
}
