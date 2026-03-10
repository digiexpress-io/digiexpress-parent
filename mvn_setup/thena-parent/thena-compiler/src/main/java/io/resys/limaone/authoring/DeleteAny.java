package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface DeleteAny {
  
  DeleteAny props(DeleteAnyProps props);
  DeleteAny props(Consumer<ImmutableDeleteAnyProps.Builder> props);
  
  Uni<Model<?>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableDeleteAnyProps.class) @JsonDeserialize(as = ImmutableDeleteAnyProps.class)
  interface DeleteAnyProps extends AuthoringModelProps {
    String getId();
    Model.BodyType getBodyType();
  }
}
