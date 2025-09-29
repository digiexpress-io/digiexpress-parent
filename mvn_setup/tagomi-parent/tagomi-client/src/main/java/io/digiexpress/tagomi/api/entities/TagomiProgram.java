package io.digiexpress.tagomi.api.entities;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface TagomiProgram {

  String getName();
  String getLocale();

  List<TagomiAst.TypeDef> getAcceptDefs();
    

  @Value.Immutable
  @JsonSerialize(as = ImmutableProgramMessage.class)
  @JsonDeserialize(as = ImmutableProgramMessage.class)
  interface ProgramMessage {
    String getId();
    String getMsg();
    @JsonIgnore
    @Nullable
    Integer getRow();
    @Nullable
    Integer getColumn();
    @Nullable
    Exception getException();
  }
}
