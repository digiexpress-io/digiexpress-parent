package io.resys.limaone.fs;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.BodyType;

@Value.Immutable
@JsonSerialize(as = ImmutableWorldFs.class) @JsonDeserialize(as = ImmutableWorldFs.class)
public interface WorldFs {
  List<DirentBase> getDirents();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableDirentBase.class) @JsonDeserialize(as = ImmutableDirentBase.class)
  interface DirentBase {
    String getId();
    String getName();
    String getFullPath();
    BodyType getType();
    List<DirentBase> getChildren();
  }
}
