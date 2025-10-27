package io.resys.thena.contract.client.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableInvPlanAlloc.class)
@JsonDeserialize(as = ImmutableInvPlanAlloc.class)
public interface InvPlanAlloc extends ContractEntity {
  UUID getId();
  UUID getInvPlanId();

  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable InvPlanAllocTransitives getTransitives();

  String getInvPlanAllocCode();
  String getInvPlanAllocName();
  BigDecimal getInvPlanAllocPercentage();
  String getInvPlanAllocStatus();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.INV_PLAN_ALLOC; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableInvPlanAllocTransitives.class)
  @JsonDeserialize(as = ImmutableInvPlanAllocTransitives.class)
  interface InvPlanAllocTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
    // Virtual field from parent table
    UUID getContractId();
  }
}