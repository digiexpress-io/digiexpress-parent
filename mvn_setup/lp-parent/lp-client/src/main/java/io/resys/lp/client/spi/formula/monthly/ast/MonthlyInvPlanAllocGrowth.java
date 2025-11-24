package io.resys.lp.client.spi.formula.monthly.ast;

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
import java.util.List;

import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

public class MonthlyInvPlanAllocGrowth {
  
  @Value
  public static class Expression {
    InvPlan invPlan;
    InvPlanAlloc allocation;
  }
  
  @Data @Builder @Jacksonized
  public static class Node {
    private final List<String> logs;
    
    private final String invPlanId;
    private final String allocationId;
    
    private final BigDecimal currentAllocationValue;
    private final BigDecimal grossGrowthAmount;
    private final BigDecimal mortalityFeeAmount;
    private final BigDecimal netGrowthAmount;
    private final BigDecimal newAllocationValue;
    
    private final BigDecimal fundUnitPrice;
  }
  
}