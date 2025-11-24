package io.resys.lp.client.spi.formula.feemi_savings.monthly.ast;

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

import java.math.BigDecimal;

import io.resys.thena.contract.client.entities.InvPlan;
import lombok.Value;

public class MonthlyInvPlanGrowth {
  
  @Value
  public static class Expression {
    InvPlan invPlan;
  }
  
  @Value
  public static class Node {
    BigDecimal grossGrowth;
    BigDecimal mortalityFees;
    BigDecimal netGrowth;
  }
  
}