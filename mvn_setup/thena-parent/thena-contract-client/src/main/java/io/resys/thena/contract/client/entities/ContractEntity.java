package io.resys.thena.contract.client.entities;

import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface ContractEntity {
  ContractDocType getDocType();

  enum ContractRelationType {
    INV_PLAN_ALLOC,
    INV_PLAN,
    COVERAGE,
    PARTY
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableContractOneOfRelations.class)
  @JsonDeserialize(as = ImmutableContractOneOfRelations.class)
  interface ContractOneOfRelations {
    @Nullable UUID getInvPlanAllocId();
    @Nullable UUID getInvPlanId();
    @Nullable UUID getCoverageId();
    @Nullable UUID getPartyId();
    ContractRelationType getRelationType();
    
    @JsonIgnore 
    default UUID getTargetId() {
      switch (getRelationType()) {
        case INV_PLAN_ALLOC: return getInvPlanAllocId();
        case INV_PLAN: return getInvPlanId();
        case COVERAGE: return getCoverageId();
        case PARTY: return getPartyId();
        default: throw new IllegalArgumentException("Unexpected value: " + getRelationType());
      }
    }
  }
}