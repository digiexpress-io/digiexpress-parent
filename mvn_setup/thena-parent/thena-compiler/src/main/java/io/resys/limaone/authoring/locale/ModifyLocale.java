package io.resys.limaone.authoring.locale;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyLocale {
  
  ModifyLocale props(ModifyLocaleProps props);
  ModifyLocale props(Consumer<ImmutableModifyLocaleProps.Builder> props);
  
  Uni<Model<Locale>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyLocaleProps.class) @JsonDeserialize(as = ImmutableModifyLocaleProps.class)
  interface ModifyLocaleProps {
    String getLocaleId();
    String getValue();
    Boolean getEnabled();
  }
}