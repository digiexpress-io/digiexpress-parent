package io.resys.crm.client.tests;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.ImmutableChangeCustomerFirstName;
import io.resys.crm.client.api.model.ImmutableCreateCustomer;
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
        .userId("user-1")
        .build())
      .await().atMost(atMost);
  }
  
  @org.junit.jupiter.api.Test
  public void updateCustomerName() {
    final var repoName = CustomerUpdateTest.class.getSimpleName() + "UpdateCustomerName";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createCustomerForUpdating(client);
    
    client.updateCustomer().updateOne(ImmutableChangeCustomerFirstName.builder()
        .userId("tester-bob")
        .customerId(customer.getId())
        .firstName("Jack")
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/updateCustomerName.txt");
  }




}
