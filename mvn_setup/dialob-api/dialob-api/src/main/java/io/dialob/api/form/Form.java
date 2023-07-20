/*
 * Copyright 2018 ReSys OÜ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.api.form;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.immutables.gson.Gson;
import org.immutables.mongo.Mongo;
import org.immutables.value.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.annotation.AllowNulls;
import io.dialob.api.annotation.Nullable;
import io.dialob.api.validation.WithValidation;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableForm.class)
@JsonDeserialize(as = ImmutableForm.class)
@Gson.TypeAdapters(emptyAsNulls = true)
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"saving","rules","updated","failed", "serviceCalls"})
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
@Mongo.Repository("forms")
public interface Form extends WithValidation<Form>, FormEntity {

  @JsonProperty("_id")
  @Gson.Named("_id")
  @Id
  @Nullable
  @Mongo.Id
  String getId();

  @JsonProperty("_rev")
  @Gson.Named("_rev")
  @Version
  @Nullable
  String getRev();

  @Nullable
  String getName();

  @Valid
  @NotNull
  Map<String, FormItem> getData();

  @Valid
  @NotNull
  Metadata getMetadata();

  @Valid
  @NotNull
  List<Variable> getVariables();

  @Valid
  @NotNull
  Map<String, Form> getNamespaces();

  @Valid
  @NotNull
  List<FormValueSet> getValueSets();

  /**
   *
   * @return error text for required fields, unless not defined per item
   */
  @NotNull
  Map<String, String> getRequiredErrorText();

  @Value.Immutable
  @Value.Style(typeImmutable = "ImmutableForm*", typeModifiable = "ModifiableForm*", validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
  @Value.Modifiable
  @JsonSerialize(as = ImmutableFormMetadata.class)
  @JsonDeserialize(as = ImmutableFormMetadata.class)
  @Gson.TypeAdapters(emptyAsNulls = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
  interface Metadata extends Serializable {

    @NotNull
    String getLabel();

    @Nullable Date getCreated();

    @Nullable Date getLastSaved();

    @Nullable Boolean getValid();

    @Nullable String getCreator();

    @Nullable String getTenantId();

    @Nullable String getSavedBy();

    @NotNull
    Set<String> getLabels();

    @Nullable String getDefaultSubmitUrl();

    @NotNull
    Set<String> getLanguages();

    @JsonInclude
    @JsonAnyGetter
    @AllowNulls
    Map<String,Object> getAdditionalProperties();
  }
}
