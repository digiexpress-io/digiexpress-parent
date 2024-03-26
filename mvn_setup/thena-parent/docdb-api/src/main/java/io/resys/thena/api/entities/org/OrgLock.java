package io.resys.thena.api.entities.org;

import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocEntity;

@Value.Immutable
public  
interface OrgLock extends DocEntity {
  enum OrgLockStatus { 
    LOCK_TAKEN, NOT_FOUND
  }
  OrgLock.OrgLockStatus getStatus();
  Optional<String> getMessage();
}