package io.resys.sysconfig.client.mig.model;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;


@Value.Immutable @JsonSerialize(as = ImmutableMigrationAssets.class) @JsonDeserialize(as = ImmutableMigrationAssets.class)
public interface MigrationAssets {
  List<String> getForms();
  String getHdes();
  String getStencil();
  CreateSysConfig getCommand();
}
