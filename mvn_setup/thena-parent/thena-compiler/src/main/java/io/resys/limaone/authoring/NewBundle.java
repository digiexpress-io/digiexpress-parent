package io.resys.limaone.authoring;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewBundle {

  
  NewBundle props(NewBundleProps props);
  NewBundle props(Consumer<ImmutableNewBundleProps.Builder> props);
  
  Uni<NewBundleProps> build();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableNewBundleProps.class)
  @JsonDeserialize(as = ImmutableNewBundleProps.class)
  public interface NewBundleProps {
    @Nullable String getStencilTag(); // auto-create tag on null
    @Nullable String getWrenchTag();  // auto-create tag on null
    
    @Nullable String getName();  // autoname on null
    @Nullable String getDescription();
    @Nullable LocalDateTime getLiveDate();
    
  }
}
