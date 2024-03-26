package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

// Resource name - blob id(content in blob)
@Value.Immutable
public
interface TreeValue extends GitEntity {
  // Name of the resource
  String getName();
  // Id of the blob that holds content
  String getBlob();
}