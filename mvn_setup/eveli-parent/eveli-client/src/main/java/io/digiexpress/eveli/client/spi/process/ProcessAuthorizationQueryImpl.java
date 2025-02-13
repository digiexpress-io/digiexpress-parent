package io.digiexpress.eveli.client.spi.process;

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
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.ImmutableProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessClient.InitProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessAuthorizationQuery;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProcessAuthorizationQueryImpl implements ProcessAuthorizationQuery {
  private final static String DT_NAME = "ProcessAuthorizationDT";  
  private final static String DT_ROLE_INPUT_NAME = "role";
  private final static String DT_ROLE_OUTPUT_NAME = "processName";  
  private final static String ROLE_SPLIT = ";";  
  
  private final EveliEnvirClient envir;
  
  @Override
  public ProcessAuthorization get(InitProcessAuthorization init) {
    final var runtime = envir.runtimeQuery().getOne().await().atMost(ProcessClientImpl.asset_setup_duration);
    return processRequest(new AuthorizationRequest(runtime, init));
  }
  
  
  @Data @RequiredArgsConstructor
  private static class AuthorizationRequest {
    private final EveliRuntime runtime;
    private final InitProcessAuthorization init;
  }
  

  private static ProcessAuthorization processRequest(AuthorizationRequest init) {
    
    final var dt = init.runtime.getWrench().decision(DT_NAME)
      .callback(ast -> {
        final var output = ast.getHeaders().getReturnDefs().stream().filter(t -> t.getName().equals(DT_ROLE_OUTPUT_NAME)).findFirst();
        final var input = ast.getHeaders().getAcceptDefs().stream().filter(t -> t.getName().equals(DT_ROLE_INPUT_NAME)).findFirst();
        ProcessAssert.isTrue(input.isPresent(), () -> "Authorizations required DT with name: " + DT_NAME + " must contain input field with name: " + DT_ROLE_INPUT_NAME + "!");
        ProcessAssert.notNull(output.isPresent(), () -> "Authorizations required DT with name: " + DT_NAME + " must contain output field with name: " + DT_ROLE_OUTPUT_NAME + "!");    
    });
    ProcessAssert.notNull(dt, () -> "Authorizations requires DT with name: " + DT_NAME + "!");
    
    final var processNames = new ArrayList<String>();
    for(final var role : init.getInit().getUserRoles()) {
      final List<String> rows = init.runtime.getWrench().inputField(DT_ROLE_INPUT_NAME, role).decision(DT_NAME).andFind()
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
}
