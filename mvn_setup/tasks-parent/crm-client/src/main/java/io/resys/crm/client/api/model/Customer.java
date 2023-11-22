package io.resys.crm.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableCustomer.class) @JsonDeserialize(as = ImmutableCustomer.class)
public interface Customer extends Document {
  String getId();
  String getExternalId(); //SSN or Business ID
  Instant getCreated();
  Instant getUpdated();
  
  CustomerBody getBody();
  
  enum CustomerBodyType {
    COMPANY, PERSON
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerBody.class) @JsonDeserialize(as = ImmutableCustomerBody.class)
  interface CustomerBody {
    String getUserName();
    CustomerBodyType getType();
    Optional<CustomerContact> getContact();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutablePerson.class) @JsonDeserialize(as = ImmutablePerson.class)
  interface Person extends CustomerBody {
   @JsonIgnore @Override @Value.Default default CustomerBodyType getType() { return CustomerBodyType.PERSON; }
   String getFirstName();
   String getLastName();
   Optional<Boolean> getProtectionOrder();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCompany.class) @JsonDeserialize(as = ImmutableCompany.class)
  interface Company extends CustomerBody {
    @JsonIgnore @Override @Value.Default default CustomerBodyType getType() { return CustomerBodyType.COMPANY; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerContact.class) @JsonDeserialize(as = ImmutableCustomerContact.class)
  interface CustomerContact {
    String getEmail();
    CustomerAddress getAddress();
    String getAddressValue();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerAddress.class) @JsonDeserialize(as = ImmutableCustomerAddress.class)
  interface CustomerAddress {
    String getLocality();
    String getStreet();
    String getPostalCode();
    String getCountry();
  }
    
  List<CustomerTransaction> getTransactions(); 
  @Value.Default default DocumentType getDocumentType() { return DocumentType.CUSTOMER; }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerTransaction.class) @JsonDeserialize(as = ImmutableCustomerTransaction.class)
  interface CustomerTransaction extends Serializable {
    String getId();
    List<CustomerCommand> getCommands(); 
  }
}
