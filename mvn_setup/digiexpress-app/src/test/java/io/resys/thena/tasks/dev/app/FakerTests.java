package io.resys.thena.tasks.dev.app;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.github.mpolla.HetuUtil;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FakerTests {

  
  @Test
  public void testNames() {
    Faker faker = new Faker(new Locale("fi-FI"));
    final var firstName = faker.name().firstName();
    final var lastName = faker.name().lastName();
    final var ssn = HetuUtil.generateRandom();
    
    final var zipCode = faker.address().zipCode();
    final var city = faker.address().city();
    final var buildingNumber = faker.address().buildingNumber();
    final var streetName = faker.address().streetName();
    // CustomerAddress
    log.debug("{} {} - {} - {}, {} {}, {}", firstName, lastName, ssn, city, streetName, buildingNumber, zipCode);
  }
}
