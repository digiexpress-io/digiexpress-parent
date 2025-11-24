package io.resys.lp.client.spi.formula;

/*-
 * #%L
 * lp-client
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

import io.resys.lp.client.api.LpClient.CalculationFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.spi.formula.monthly.Monthly_FeemiSavings;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MonthlyCalculationFactory implements CalculationFormula {
  
  @Override
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {

    final var resolver = container.getContract().getContract().getContractSubType().orElse("");
    
    switch (resolver) {
      case "FEEMI_SAVINGS": return new Monthly_FeemiSavings().accept(container);
      default: throw new IllegalArgumentException("Unexpected value for monthly calculation: " + resolver);
    }
  }  

}