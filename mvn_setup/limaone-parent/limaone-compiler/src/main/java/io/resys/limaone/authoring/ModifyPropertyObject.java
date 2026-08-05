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

public interface ModifyPropertyObject {

  ModifyPropertyObject props(ModifyPropertyObjectProps props);
  ModifyPropertyObject props(Consumer<ImmutableModifyPropertyObjectProps.Builder> props);

  Uni<Model<PropertyObject>> build();
  Model<PropertyObject> buildSync();


  @Value.Immutable 
  @JsonSerialize(as = ImmutableModifyPropertyObjectProps.class) 
  @JsonDeserialize(as = ImmutableModifyPropertyObjectProps.class)
  interface ModifyPropertyObjectProps extends AuthoringModelProps {
    String getPropertyObjectId();
    @Nullable String getContent();
  }
}
