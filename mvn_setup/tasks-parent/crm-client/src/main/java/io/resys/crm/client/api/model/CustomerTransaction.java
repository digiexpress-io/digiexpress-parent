package io.resys.crm.client.api.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableCustomerTransaction.class) @JsonDeserialize(as = ImmutableCustomerTransaction.class)
public
interface CustomerTransaction extends Serializable {
  String getId();
  List<CustomerCommand> getCommands(); 
  
  String getUserId();
  Instant getTargetDate();
}