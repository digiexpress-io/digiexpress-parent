package io.resys.thena.projects.client.api;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableTenantConfigTransaction.class) @JsonDeserialize(as = ImmutableTenantConfigTransaction.class)
public
interface TenantConfigTransaction extends Serializable {
  String getId();
  List<TenantConfigCommand> getCommands(); 
}