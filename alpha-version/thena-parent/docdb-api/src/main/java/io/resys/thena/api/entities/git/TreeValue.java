package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

// Resource name - blob id(content in blob)
@Value.Immutable
public
interface TreeValue extends GitEntity, ThenaTable {
  // Name of the resource
  String getName();
  // Id of the blob that holds content
  String getBlob();
}