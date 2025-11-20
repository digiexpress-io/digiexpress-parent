package io.resys.lp.client.spi.realcalculation;

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

import java.time.LocalDate;

import io.resys.lp.client.api.LpClient.RealCalculation;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.smallrye.mutiny.Uni;

public class MonethlyRealCalculation implements RealCalculation {

  @Override
  public RealCalculation accountId(String contractIdOrRefOrEtc) {
    return this;
  }

  @Override
  public RealCalculation startDate(LocalDate localDate) {
    return this;
  }

  @Override
  public Uni<Envelope<AnyCalculation>> build() {
    // TODO Auto-generated method stub
    return null;
  }
}
