package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

@Value.Immutable
public
interface BlobHistory extends GitEntity {
  String getTreeId();
  String getTreeValueName();
  String getCommit();
  Blob getBlob();
}