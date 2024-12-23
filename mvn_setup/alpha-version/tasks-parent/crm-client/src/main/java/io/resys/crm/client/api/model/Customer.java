package io.resys.crm.client.api.model;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableCustomer.class) @JsonDeserialize(as = ImmutableCustomer.class)
public interface Customer {
  String getId();
  String getExternalId(); //SSN or Business ID
  @Nullable String getVersion();
  @Nullable Instant getCreated();
  @Nullable Instant getUpdated();
  CustomerBody getBody();
  List<CustomerTransaction> getTransactions(); 
  
  enum CustomerBodyType {
    COMPANY, PERSON
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "type")
  @JsonSubTypes({
    @Type(value = ImmutableCompany.class, name = "COMPANY"),  
    @Type(value = ImmutablePerson.class, name = "PERSON"),  
  })
  interface CustomerBody {
    String getUsername();
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
}
