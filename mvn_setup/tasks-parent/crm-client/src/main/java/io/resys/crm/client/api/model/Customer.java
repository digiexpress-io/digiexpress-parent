package io.resys.crm.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableCustomer.class) @JsonDeserialize(as = ImmutableCustomer.class)
public interface Customer extends Document {
  String getId();
  
  String getName();
  
  Instant getCreated();
  Instant getUpdated();
  
  
  List<CustomerTransaction> getTransactions(); 
  @Value.Default default DocumentType getDocumentType() { return DocumentType.CRM; }


  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerTransaction.class) @JsonDeserialize(as = ImmutableCustomerTransaction.class)
  interface CustomerTransaction extends Serializable {
    String getId();
    List<CustomerCommand> getCommands(); 
  }
}
