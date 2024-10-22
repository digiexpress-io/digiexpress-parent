package io.digiexpress.eveli.client.spi;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.ImmutableProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessAuthorizationCommands;
import io.digiexpress.eveli.client.spi.asserts.WorkflowAssert;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class ProcessAuthorizationCommandsImpl implements ProcessAuthorizationCommands {
  
  private final HdesClient hdesClient;
  private final Supplier<ProgramEnvir> programEnvir;
  private final static String DT_NAME = "ProcessAuthorizationDT";  
  private final static String DT_ROLE_INPUT_NAME = "role";
  private final static String DT_ROLE_OUTPUT_NAME = "processName";  
  private final static String ROLE_SPLIT = ";";  
  
  @Data @RequiredArgsConstructor
  private static class AuthorizationRequest {
    private final HdesClient hdesClient;
    private final ProgramEnvir programEnvir;
    private final InitProcessAuthorization init;
  }
  
  @Override
  public ProcessAuthorizationQuery query() {
    return new ProcessAuthorizationQuery() {
      @Override
      public ProcessAuthorization get(InitProcessAuthorization init) {
        return processRequest(new AuthorizationRequest(hdesClient, programEnvir.get(), init));
      }
    };
  }
  
  private static ProcessAuthorization processRequest(AuthorizationRequest init) {
    final var dt = init.programEnvir.getDecisionsByName().get(DT_NAME);
    WorkflowAssert.notNull(dt, () -> "Authorizations requires DT with name: " + DT_NAME + "!");
    WorkflowAssert.isTrue(dt.getStatus() == ProgramStatus.UP, () -> "Authorizations required DT with name: " + DT_NAME + " has compilation errors!");
    final var ast = dt.getAst().get();
    
    final var output = ast.getHeaders().getReturnDefs().stream().filter(t -> t.getName().equals(DT_ROLE_OUTPUT_NAME)).findFirst();
    final var input = ast.getHeaders().getAcceptDefs().stream().filter(t -> t.getName().equals(DT_ROLE_INPUT_NAME)).findFirst();
    WorkflowAssert.isTrue(input.isPresent(), () -> "Authorizations required DT with name: " + DT_NAME + " must contain input field with name: " + DT_ROLE_INPUT_NAME + "!");
    WorkflowAssert.notNull(output.isPresent(), () -> "Authorizations required DT with name: " + DT_NAME + " must contain output field with name: " + DT_ROLE_OUTPUT_NAME + "!");    
    
    final var processNames = new ArrayList<String>();
    for(final var role : init.getInit().getUserRoles()) {
      final List<String> rows = init.hdesClient.executor(init.programEnvir).inputField(DT_ROLE_INPUT_NAME, role).decision(DT_NAME).andFind()
          .stream().flatMap(row -> {
            final var outputName = row.get(DT_ROLE_OUTPUT_NAME);
            if(outputName == null) {
              return new ArrayList<String>().stream();
            }
            return Arrays.asList(outputName.toString().split(ROLE_SPLIT)).stream();
          })
          .collect(Collectors.toList());
      processNames.addAll(rows);
    }
    
    return ImmutableProcessAuthorization.builder()
        .addAllAllowedProcessNames(processNames.stream().map(e -> e.trim()).distinct().collect(Collectors.toList()))
        .userRoles(init.init.getUserRoles())
        .build();
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private HdesClient hdesClient;
    private Supplier<ProgramEnvir> programEnvir;

    public ProcessAuthorizationCommandsImpl build() {
      WorkflowAssert.notNull(hdesClient, () -> "hdesClient must be defiend!");
      WorkflowAssert.notNull(programEnvir, () -> "programEnvir must be defiend!");
      return new ProcessAuthorizationCommandsImpl(hdesClient, programEnvir);
    }
  }
}
