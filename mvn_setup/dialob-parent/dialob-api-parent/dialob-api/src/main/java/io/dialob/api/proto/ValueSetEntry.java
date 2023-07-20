package io.dialob.api.proto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.Nullable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
@JsonSerialize(as = ImmutableValueSetEntry.class)
@JsonDeserialize(as = ImmutableValueSetEntry.class)
@Gson.TypeAdapters
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Style(allParameters = true)
public interface ValueSetEntry extends Serializable {

  String getKey();

  @Nullable
  String getValue();

}
