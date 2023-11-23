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
        .targetDate(getTargetDate())
        .body(ImmutablePerson.builder()
            .userName("Amanda Smith")
            .firstName("something first")
            .lastName("something last")
            .type(CustomerBodyType.PERSON)
            .contact(ImmutableCustomerContact.builder()
                .address(ImmutableCustomerAddress.builder()
                    .country("FI")
                    .locality("Helsinki")
                    .postalCode("12345")
                    .street("1234 Any street")
                    .build())
                .email("ron@gmail.com")
                .addressValue("1234 Any street, Helsinki, FI, 12345")
                .build())
            .build())
        .externalId("external-id")
        .userId("tester-bob")
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
        .targetDate(getTargetDate())
        .userId("jane.doe@morgue.com")
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/create-customer-change-first-name.txt");
  }
  

  /*
  
  @org.junit.jupiter.api.Test
  public void upsertSuomiFiPerson() {
    final var repoName = CustomerUpdateTest.class.getSimpleName() + "UpsertSuomiFiPerson";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createCustomerForUpdating(client);
    
    client.updateCustomer().updateOne(ImmutableUpsertSuomiFiPerson.builder()
        .userId("tester-bob")
        .userName("George Wallace")
        .customerId(customer.getExternalId())
        .firstName("Jack")
        .lastName("Brachus")
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/upsert-suomi-fi-person.txt");
  }
*/


}
