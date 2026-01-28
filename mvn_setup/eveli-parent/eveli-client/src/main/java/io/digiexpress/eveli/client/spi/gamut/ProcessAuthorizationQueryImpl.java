package io.digiexpress.eveli.client.spi.gamut;

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
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.GamutClient.ProcessAuthorization;
import io.digiexpress.eveli.client.api.GamutClient.ProcessAuthorizationQuery;
import io.digiexpress.eveli.client.api.ImmutableProcessAuthorization;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitIdSupplier;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProcessAuthorizationQueryImpl implements ProcessAuthorizationQuery {
  private final static String DT_NAME = "ProcessAuthorizationDT";  
  private final static String DT_ROLE_INPUT_NAME = "role";
  private final static String DT_ROLE_OUTPUT_NAME = "processName";  
  private final static String ROLE_SPLIT = ";";  
  
  private final EveliEnvirClient envir;
  
  private String cockpitId;
  private final List<String> userRoles = new ArrayList<>();

  @Data @RequiredArgsConstructor
  private static class AuthorizationRequest {
    private final EveliRuntime runtime;
    private final @Nullable String cockpitId;
    private final List<String> userRoles;
  }
  

  @Override
  public ProcessAuthorizationQuery cockpitId(String cockpitId) {
    this.cockpitId = cockpitId;
    return this;
  }
  @Override
  public ProcessAuthorizationQuery userRoles(List<String> userRoles) {
    this.userRoles.addAll(userRoles);
    return this;
  }
  
  @Override
  public Uni<ProcessAuthorization> getOne() {
    final CockpitIdSupplier cockpitIdSupplier = () -> Uni.createFrom().item(Optional.ofNullable(cockpitId));
    return envir.withCockpitIdSupplier(cockpitIdSupplier).runtimeQuery()
        .getOne().onItem().transform(runtime -> processRequest(new AuthorizationRequest(runtime, cockpitId, userRoles)));
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
    for(final var role : init.getUserRoles()) {
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
        .userRoles(init.getUserRoles())
        .build();
  }
}
