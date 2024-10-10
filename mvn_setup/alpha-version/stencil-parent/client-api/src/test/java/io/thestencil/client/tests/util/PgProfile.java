package io.thestencil.client.tests.util;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class PgProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
      "quarkus.datasource.db-kind", "pg",
      "quarkus.datasource.reactive.max-size", "20"
    );
  }

  @Override
  public String getConfigProfile() {
    return "pg-profile";
  }
}
