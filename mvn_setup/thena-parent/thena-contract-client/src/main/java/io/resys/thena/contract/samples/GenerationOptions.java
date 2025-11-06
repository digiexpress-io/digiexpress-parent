package io.resys.thena.contract.samples;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.product.client.api.Product.AgeRange;
import io.resys.thena.product.client.api.Product.IncomeRange;

@Value.Immutable
@JsonSerialize(as = ImmutableGenerationOptions.class)
@JsonDeserialize(as = ImmutableGenerationOptions.class)
public interface GenerationOptions {
  
  AgeRange getAgeRange();
  IncomeRange getIncomeRange();
  
  @Value.Default
  default boolean isIncludeBeneficiaries() { return true; }
  
  @Value.Default
  default String getRiskProfile() { return "MODERATE"; }
  
  
  static ImmutableGenerationOptions.Builder builder() {
    return ImmutableGenerationOptions.builder();
  }
  
  static GenerationOptions defaults() {
    return builder().build();
  }
  

}