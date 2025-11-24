package io.resys.lp.client.api.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.immutables.value.Value;


@Value.Immutable
public interface Fund {
  
  String getId();
  FundValue getCalculationValue();
  List<FundValue> getRelevantValues();
  

  @Value.Immutable
  interface FundValue {
    LocalDate getPriceDate();
    BigDecimal getPriceValue();
    FundValueType getPriceType();
  }
  
  enum FundValueType {
    ESTIMATE, REAL
  }
}
