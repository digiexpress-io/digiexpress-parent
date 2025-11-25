package io.resys.lp.client.spi;

import java.time.LocalDate;

import io.resys.lp.client.api.LpClient.FundQuery;
import io.resys.lp.client.api.entities.Fund;
import io.resys.lp.client.api.entities.Fund.FundValueType;
import io.resys.lp.client.api.entities.ImmutableFund;
import io.resys.lp.client.api.entities.ImmutableFundValue;
import io.resys.lp.product.spi.providers.Fund_Provider;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FundQueryImpl implements FundQuery {
  
  private final LedgerContainer ledger;

  @Override
  public Fund getOne(String fundIdNameOrCode, LocalDate targetDate) {
    RepoAssert.notEmpty(fundIdNameOrCode, () -> "Fund name/id/code must be provided!");
    RepoAssert.notNull(targetDate, () -> "targetDate must be defined!");
    
    // 1. Find fund info from Fund_Provider for metadata
    final var fundInfo = findFundByNameOrCode(fundIdNameOrCode);
    final var fundId = fundInfo.getCode();
    
    // 2. Get unit price from ledger for target date (or closest past date)
    final var unitPrice = ledger.getUnitPrices().stream()
        .filter(up -> fundId.equals(up.getFundId()))
        .filter(up -> !up.getUnitDate().isAfter(targetDate)) // Not in future
        .max((a, b) -> a.getUnitDate().compareTo(b.getUnitDate())) // Latest available
        .orElse(null);
    
    if (unitPrice == null) {
      throw new IllegalArgumentException("No unit price found for fund: " + fundId + " on or before: " + targetDate);
    }
    
    // 3. Create calculation value from ledger data
    final var calculationValue = ImmutableFundValue.builder()
        .priceDate(unitPrice.getUnitDate())
        .priceValue(unitPrice.getUnitValue())
        .priceType(FundValueType.REAL) // From ledger, so it's real data
        .build();
    
    // 4. Get recent historical values (last 5 trading days for context)
    final var historicalValues = ledger.getUnitPrices().stream()
        .filter(up -> fundId.equals(up.getFundId()))
        .filter(up -> up.getUnitDate().isBefore(unitPrice.getUnitDate()))
        .sorted((a, b) -> b.getUnitDate().compareTo(a.getUnitDate())) // Latest first
        .limit(5)
        .map(up -> ImmutableFundValue.builder()
            .priceDate(up.getUnitDate())
            .priceValue(up.getUnitValue())
            .priceType(FundValueType.REAL)
            .build())
        .toList();
    
    return ImmutableFund.builder()
        .id(fundId)
        .calculationValue(calculationValue)
        .addAllRelevantValues(historicalValues)
        .build();
  }
  
  private Fund_Provider.FundInfo findFundByNameOrCode(String nameOrCode) {
    if (nameOrCode == null) {
      return Fund_Provider.FUND_INFO[1]; // Default to TASAPAINOINEN_RAHASTO
    }
    
    // Direct match first (code or name - case insensitive)
    for (Fund_Provider.FundInfo fundInfo : Fund_Provider.FUND_INFO) {
      if (fundInfo.getCode().equalsIgnoreCase(nameOrCode) || 
          fundInfo.getName().equalsIgnoreCase(nameOrCode)) {
        return fundInfo;
      }
    }
    
    return Fund_Provider.FUND_INFO[1]; // Default to balanced fund
  }
  
}
