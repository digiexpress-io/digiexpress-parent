package io.resys.limaone.program;

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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Parameter;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ProgramInput extends Serializable {
  Serializable getValue(Parameter parameter);
  ResolvedParameter getValueWithMeta(String name);
  ProgramInput withInputs(Map<String, Serializable> nextInputs);
  
  
  @Value.Immutable
  interface ResolvedParameter {
    boolean getFound();
    @Nullable Serializable getValue();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableParticipantForm.class) @JsonDeserialize(as = ImmutableParticipantForm.class)
  interface ParticipantForm {
    String getQuestionnaireId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableParticipant.class) @JsonDeserialize(as = ImmutableParticipant.class)
  interface Participant  {
    // even anon user needs to have some anon identity and some primitive roles
    String getIdentity();
    ParticipantId getPartId();
    String getUsername();
    List<String> getIdentityRoles();
    
    Boolean getAnon(); 
    Boolean getProtectionOrder();
    
    // Anything relevant to pass downstream
    @Nullable JsonObject getAdditionalProps();
    
    // Loose data    
    @Nullable String getCompanyName();
    @Nullable String getFirstName();
    @Nullable String getLastName();
    @Nullable String getLanguage();
    @Nullable String getEmail();
    @Nullable String getAddress();

    @Nullable String getRepresentativeUsername();
    @Nullable String getRepresentativeFirstName();
    @Nullable String getRepresentativeLastName();
    @Nullable String getRepresentativeIdentity(); 
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableParticipantId.class) @JsonDeserialize(as = ImmutableParticipantId.class)
  interface ParticipantId {
    String getHashId();
    String getRealId();
  }
}
