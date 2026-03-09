package io.resys.limaone.authoring;


import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;



public interface NewLocale {

  NewLocale props(NewLocaleProps props);
  NewLocale props(Consumer<ImmutableNewLocaleProps.Builder> props);
  
  Uni<Model<Locale>> build();
  Model<Locale> buildSync();
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewLocaleProps.class) @JsonDeserialize(as = ImmutableNewLocaleProps.class)
  interface NewLocaleProps extends AuthoringModelProps {
    String getLocale();
    
    @Nullable String getId();
  }
}