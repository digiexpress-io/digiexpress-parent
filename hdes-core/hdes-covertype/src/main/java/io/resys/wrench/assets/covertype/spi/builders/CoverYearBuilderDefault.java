package io.resys.wrench.assets.covertype.spi.builders;

import java.util.Optional;

import io.resys.wrench.assets.covertype.api.CoverYearBuilder;
import io.resys.wrench.assets.covertype.api.ImmutableCoverYear;

public class CoverYearBuilderDefault implements CoverYearBuilder {

  private Integer daysInMonth;
  private Integer daysInYear;
  
  @Override
  public CoverYearBuilder daysInMonth(int daysInMonth) {
    this.daysInMonth = daysInMonth;
    return this;
  }
  @Override
  public CoverYearBuilder daysInYear(int daysInYear) {
    this.daysInYear = daysInYear;
    return this;
  }
  @Override
  public CoverYear build() {
    return ImmutableCoverYear.builder()
        .daysInMonth(Optional.ofNullable(daysInMonth))
        .daysInYear(Optional.ofNullable(daysInYear))
        .year(daysInYear == null ? YearType.NATURAL : YearType.CUSTOM)
        .month(daysInMonth == null ? MonthType.NATURAL : MonthType.CUSTOM)
        .build();
  }
}
