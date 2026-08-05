package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.PropertyObject;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewPropertyObject {

  NewPropertyObject props(NewPropertyObjectProps props);
  NewPropertyObject props(Consumer<ImmutableNewPropertyObjectProps.Builder> props);

  Uni<Model<PropertyObject>> build();
  Model<PropertyObject> buildSync();


  @Value.Immutable
  @JsonSerialize(as = ImmutableNewPropertyObjectProps.class)
  @JsonDeserialize(as = ImmutableNewPropertyObjectProps.class)
  interface NewPropertyObjectProps extends AuthoringModelProps {
    String getName();
    String getObjectType();
    @Nullable String getContent();
    
  }
}
