package io.resys.crm.client.tests;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.Customer.CustomerBodyType;
import io.resys.crm.client.api.model.ImmutableChangeCustomerFirstName;
import io.resys.crm.client.api.model.ImmutableCreateCustomer;
import io.resys.crm.client.api.model.ImmutableCustomerAddress;
import io.resys.crm.client.api.model.ImmutableCustomerContact;
import io.resys.crm.client.api.model.ImmutablePerson;
import io.resys.crm.client.api.model.ImmutableUpsertSuomiFiPerson;
import io.resys.crm.client.tests.config.CrmPgProfile;
import io.resys.crm.client.tests.config.CustomerTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(CrmPgProfile.class)
public class CustomerUpdateTest extends CustomerTestCase {

  private Customer createCustomerForUpdating(CrmClient client) {
    return client.createCustomer()
      .createOne(ImmutableCreateCustomer.builder()
        .body(ImmutablePerson.builder()
            .username("Amanda Smith")
            .firstName("Amanda")
            .lastName("Smith")
            .type(CustomerBodyType.PERSON)
            .contact(ImmutableCustomerContact.builder()
                .address(ImmutableCustomerAddress.builder()
                    .country("SV")
                    .locality("Stockholm")
                    .postalCode("79773")
                    .street("56 Main street")
                    .build())
                .email("customer@gmail.com")
                .addressValue("1234 Any street, Helsinki, FI, 12345")
                .build())
            .build())
        .externalId("external-id")
        .build())
      .await().atMost(atMost);
  }
  
  @org.junit.jupiter.api.Test
  public void changeCustomerFirstName() {
    final var repoName = CustomerUpdateTest.class.getSimpleName() + "ChangeCustomerFirstName";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createCustomerForUpdating(client);
    
    client.updateCustomer().updateOne(ImmutableChangeCustomerFirstName.builder()
        .firstName("Jack")
        .customerId(customer.getExternalId())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/create-customer-change-first-name.txt");
  }
  
  
  @org.junit.jupiter.api.Test
  public void upsertSuomiFiPerson() {
    final var repoName = CustomerUpdateTest.class.getSimpleName() + "UpsertSuomiFiPerson";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createCustomerForUpdating(client);
    
    client.updateCustomer().updateOne(ImmutableUpsertSuomiFiPerson.builder()
        .userName("Jack Brachus")
        .customerId(customer.getExternalId())
        .protectionOrder(true)
        .firstName("Jack")
        .lastName("Brachus")
        .contact(ImmutableCustomerContact.builder()
            .address(ImmutableCustomerAddress.builder()
                .country("FI")
                .locality("Helsinki")
                .street("1234 My street")
                .postalCode("12345")
                .build())
            .addressValue("1234 My street, Helsinki, FI, 12345")
            .email("suomi-fi-customer@gmail.com")
            .build())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/upsert-suomi-fi-person.txt");
  }

  @org.junit.jupiter.api.Test
  public void upsertSuomiFiPersonChangeAddress() {
    final var repoName = CustomerUpdateTest.class.getSimpleName() + "UpsertSuomiFiPersonChangeAddress";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createCustomerForUpdating(client);
    
    client.updateCustomer().updateOne(ImmutableUpsertSuomiFiPerson.builder()
        .userName("Jack Brachus")
        .customerId(customer.getExternalId())
        .protectionOrder(true)
        .firstName("Jack")
        .lastName("Brachus")
        .contact(ImmutableCustomerContact.builder()
            .address(ImmutableCustomerAddress.builder()
                .country("FI")
                .locality("Sipoo")
                .street("35 Lake Avenue")
                .postalCode("85477")
                .build())
            .addressValue("35 Lake Avenue, Sipoo, FI, 85477")
            .email("suomi-fi-customer@gmail.com")
            .build())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/upsert-suomi-fi-person-change-address.txt");
  }


}
