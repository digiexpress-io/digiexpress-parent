package io.dialob.api.form;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.AllowNulls;
import io.dialob.api.annotation.Nullable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Value.Immutable
@Value.Style(jdkOnly = true)
@Value.Modifiable
@JsonSerialize(as = ImmutableFormValueSetEntry.class)
@JsonDeserialize(as = ImmutableFormValueSetEntry.class)
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_NULL)
@Gson.TypeAdapters(emptyAsNulls = true)
public interface FormValueSetEntry extends Serializable {

  @NotNull
  String getId();

  Map<String, String> getLabel();

  @Nullable
  String getWhen();

  @JsonInclude
  @JsonAnyGetter
  @AllowNulls
  @Gson.Ignore
  Map<String, Object> getAdditionalProperties();



}
