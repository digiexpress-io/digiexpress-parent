package io.resys.thena.contract.samples.providers;

/*-
 * #%L
 * thena-contract-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class CRM_Provider {
  
  private static final Random RANDOM = new Random();
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Faker FAKER = new Faker(Locale.of("fi", "FI"));
  
  
  private static final String[] BANKS = {
    "Nordea", "OP Ryhmä", "Danske Bank", "Handelsbanken", "Säästöpankki", "POP Pankki"
  };
  
  @Data
  @Builder
  @Jacksonized
  public static class Person {
    private String personalId;
    private String firstName;
    private String lastName;
    private String fullName;
    private boolean isMale;
    private int age;
    private LocalDate dateOfBirth;
    private Address address;
    private Contact contact;
    private Employment employment;
    private Banking banking;
    private String riskTolerance;
    private String investmentExperience;
    
    public JsonObject toJson() {
      try {
        String json = MAPPER.writeValueAsString(this);
        return new JsonObject(json);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to convert Person to JSON", e);
      }
    }
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("personalId", personalId);
      map.put("fullName", fullName);
      map.put("dateOfBirth", dateOfBirth.toString());
      map.put("address", address.toMap());
      map.put("contact", contact.toMap());
      map.put("employment", employment.toMap());
      map.put("banking", banking.toMap());
      map.put("riskTolerance", riskTolerance);
      map.put("investmentExperience", investmentExperience);
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class Address {
    private String street;
    private String postalCode;
    private String city;
    private String country;
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("street", street);
      map.put("postalCode", postalCode);
      map.put("city", city);
      map.put("country", country);
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class Contact {
    private String phone;
    private String email;
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("phone", phone);
      map.put("email", email);
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class Employment {
    private String status;
    private int annualIncome;
    private String employer;
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("status", status);
      map.put("annualIncome", annualIncome);
      map.put("employer", employer);
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class Banking {
    private String iban;
    private String bankName;
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("iban", iban);
      map.put("bankName", bankName);
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class Beneficiary {
    private String personalId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String relationship;
    private String relationshipType;
    private int percentage;
    private boolean maritalRightsExcluded;
    private Address address;
    
    public JsonObject toJson() {
      try {
        String json = MAPPER.writeValueAsString(this);
        return new JsonObject(json);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to convert Beneficiary to JSON", e);
      }
    }
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("personalId", personalId);
      map.put("fullName", fullName);
      map.put("relationship", relationship);
      map.put("relationshipType", relationshipType);
      map.put("percentage", percentage);
      map.put("maritalRightsExcluded", maritalRightsExcluded);
      map.put("address", address.toMap());
      return map;
    }
  }
  
  public static Person generatePerson(int targetAge, int minIncome, int maxIncome) {
    // Generate names using JavaFaker
    String firstName = FAKER.name().firstName();
    String lastName = FAKER.name().lastName();
    String fullName = firstName + " " + lastName;
    
    // Determine gender from first name (approximation - JavaFaker doesn't expose gender directly)
    boolean isMale = isLikelyMaleName(firstName);
    
    int birthYear = LocalDate.now().getYear() - targetAge;
    LocalDate dateOfBirth = LocalDate.now().minusYears(targetAge).minusDays(RANDOM.nextInt(365));
    String personalId = generateHenkilotunnus(birthYear, isMale);
    
    // Generate address using JavaFaker
    Address address = Address.builder()
        .street(FAKER.address().streetAddress())
        .postalCode(FAKER.address().zipCode())
        .city(FAKER.address().city())
        .country("Finland")
        .build();
    
    // Generate contact using JavaFaker
    Contact contact = Contact.builder()
        .phone(FAKER.phoneNumber().phoneNumber())
        .email(FAKER.internet().emailAddress(firstName.toLowerCase() + "." + lastName.toLowerCase()))
        .build();
    
    int annualIncome = minIncome + RANDOM.nextInt(maxIncome - minIncome + 1);
    Employment employment = Employment.builder()
        .status(generateEmploymentStatus())
        .annualIncome(annualIncome)
        .employer(FAKER.company().name())
        .build();
    
    Banking banking = Banking.builder()
        .iban(generateIban())
        .bankName(BANKS[RANDOM.nextInt(BANKS.length)])
        .build();
    
    return Person.builder()
        .personalId(personalId)
        .firstName(firstName)
        .lastName(lastName)
        .fullName(fullName)
        .isMale(isMale)
        .age(targetAge)
        .dateOfBirth(dateOfBirth)
        .address(address)
        .contact(contact)
        .employment(employment)
        .banking(banking)
        .riskTolerance(generateRiskTolerance())
        .investmentExperience(generateInvestmentExperience())
        .build();
  }
  
  public static Beneficiary generateBeneficiary(String relationship, int percentage) {
    // Generate names using JavaFaker
    String firstName = FAKER.name().firstName();
    String lastName = FAKER.name().lastName();
    String fullName = firstName + " " + lastName;
    boolean isMale = isLikelyMaleName(firstName);
    
    int age = generateBeneficiaryAge(relationship);
    int birthYear = LocalDate.now().getYear() - age;
    String personalId = generateHenkilotunnus(birthYear, isMale);
    
    // Generate address using JavaFaker
    Address address = Address.builder()
        .street(FAKER.address().streetAddress())
        .postalCode(FAKER.address().zipCode())
        .city(FAKER.address().city())
        .country("Finland")
        .build();
    
    return Beneficiary.builder()
        .personalId(personalId)
        .firstName(firstName)
        .lastName(lastName)
        .fullName(fullName)
        .relationship(relationship)
        .relationshipType(determineRelationshipType(relationship))
        .percentage(percentage)
        .maritalRightsExcluded(false)
        .address(address)
        .build();
  }
  
  // Helper methods
  private static boolean isLikelyMaleName(String firstName) {
    // Simple heuristic for Finnish names - this is approximate
    // More sophisticated approach would use a gender name database
    String lowerName = firstName.toLowerCase();
    
    // Common Finnish male endings
    if (lowerName.endsWith("i") || lowerName.endsWith("o") || lowerName.endsWith("u") || 
        lowerName.endsWith("ki") || lowerName.endsWith("ti") || lowerName.endsWith("ri")) {
      return true;
    }
    
    // Common Finnish female endings  
    if (lowerName.endsWith("a") || lowerName.endsWith("ä") || lowerName.endsWith("la") || 
        lowerName.endsWith("na") || lowerName.endsWith("ta") || lowerName.endsWith("nen")) {
      return false;
    }
    
    // Default to random if uncertain
    return RANDOM.nextBoolean();
  }
  
  private static String generateHenkilotunnus(int birthYear, boolean isMale) {
    int day = 1 + RANDOM.nextInt(28);
    int month = 1 + RANDOM.nextInt(12);
    int yearLastTwo = birthYear % 100;
    
    char centuryChar = birthYear >= 2000 ? 'A' : birthYear >= 1900 ? '-' : '+';
    int individual = 1 + RANDOM.nextInt(899);
    
    // Adjust for gender (odd=male, even=female)
    if (isMale && individual % 2 == 0) individual++;
    if (!isMale && individual % 2 == 1) individual++;
    
    char checkChar = "0123456789ABCDEFHJKLMNPRSTUVWXY".charAt(RANDOM.nextInt(31));
    
    return String.format("%02d%02d%02d%c%03d%c", day, month, yearLastTwo, centuryChar, individual, checkChar);
  }
  
  
  private static String generateIban() {
    String checkDigits = String.format("%02d", 10 + RANDOM.nextInt(90));
    String bankCode = String.format("%04d", 1000 + RANDOM.nextInt(9000));
    String accountNumber = String.format("%010d", Math.abs(RANDOM.nextLong()) % 10000000000L);
    return "FI" + checkDigits + bankCode + accountNumber;
  }
  
  private static String generateEmploymentStatus() {
    // Use JavaFaker business options where possible
    String[] statuses = {"EMPLOYED", "SELF_EMPLOYED", "UNEMPLOYED", "RETIRED", "STUDENT", "ENTREPRENEUR"};
    return statuses[RANDOM.nextInt(statuses.length)];
  }
  
  private static String generateRiskTolerance() {
    String[] tolerances = {"CONSERVATIVE", "MODERATE", "AGGRESSIVE", "VERY_AGGRESSIVE"};
    return tolerances[RANDOM.nextInt(tolerances.length)];
  }
  
  private static String generateInvestmentExperience() {
    String[] experiences = {"BEGINNER", "MODERATE", "EXPERIENCED", "EXPERT"};
    return experiences[RANDOM.nextInt(experiences.length)];
  }
  
  private static int generateBeneficiaryAge(String relationship) {
    return switch (relationship) {
      case "SPOUSE" -> 25 + RANDOM.nextInt(50);
      case "CHILD" -> 5 + RANDOM.nextInt(35);
      case "PARENT" -> 50 + RANDOM.nextInt(30);
      case "SIBLING" -> 20 + RANDOM.nextInt(50);
      default -> 18 + RANDOM.nextInt(60);
    };
  }
  
  private static String determineRelationshipType(String relationship) {
    return switch (relationship) {
      case "SPOUSE", "CHILD", "PARENT" -> "NEXT_OF_KIN";
      default -> "OTHER";
    };
  }
}