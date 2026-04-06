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

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;



public class Add_Imports extends CompilationCustomizer {

  public Add_Imports() {
    super(CompilePhase.CONVERSION);
  }


  @Override
  public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
    
    // can't cache nested types, they will work only for first compile instance .... types are mutable!!!
    final List<ClassNode> imports = Arrays.asList(
      ClassHelper.make(io.resys.limaone.model.FlowTask.ServiceData.class.getCanonicalName()),
      ClassHelper.make(io.resys.limaone.model.FlowTask.ServiceRefs.class.getCanonicalName()),
      ClassHelper.make(io.resys.limaone.model.FlowTask.ServiceRef.class.getCanonicalName()),
      
      ClassHelper.make(java.time.LocalDate.class),
      ClassHelper.make(java.time.Period.class),
      ClassHelper.make(java.time.format.DateTimeFormatter.class),
      ClassHelper.make(java.time.DayOfWeek.class),
      ClassHelper.make(java.time.Duration.class),
      ClassHelper.make(java.util.Collections.class),
      ClassHelper.make(java.util.ArrayList.class),
      ClassHelper.make(java.util.Arrays.class),
      ClassHelper.make(java.util.List.class),
      ClassHelper.make(java.util.Optional.class),
      ClassHelper.make(java.util.Objects.class),
      ClassHelper.make(java.io.Serializable.class),
  
      ClassHelper.make(io.smallrye.mutiny.Uni.class),
      ClassHelper.make(io.smallrye.mutiny.Multi.class),
  
      ClassHelper.make(io.resys.limaone.program.Runtime.class),
      ClassHelper.make(io.resys.limaone.program.ProgramInput.class),
  
      ClassHelper.make(io.resys.limaone.model.FlowTask.class)
    );
    
    final var ast = source.getAST();
    for(final var node : imports) {
      
      if(ast.getImport(node.getNameWithoutPackage()) == null) {
        ast.addImport(node.getNameWithoutPackage(), node);
      }
    }
  }
}
