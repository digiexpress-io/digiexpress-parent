package io.resys.hdes.executor.spi.operations;

/*-
 * #%L
 * hdes-executor
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HdesOperations {
  
  interface MathOperations<T extends Serializable> {
    T min();
    T max();
    T sum();
    BigDecimal avg();
  }
  
  interface MathOpetaionsBuilder {
    MathOpetaionsBuilder decimal(BigDecimal ... values);
    MathOpetaionsBuilder decimal(Collection<BigDecimal> values);
    MathOpetaionsBuilder decimal(Optional<BigDecimal> value);
    MathOpetaionsBuilder integer(Collection<Integer> values);
    MathOpetaionsBuilder integer(Optional<Integer> value);
    MathOpetaionsBuilder integer(Integer ... values);
    MathOpetaionsBuilder any(Object any);
    MathOperations<BigDecimal> toDecimal();
    MathOperations<Integer> toInteger();
    MathOperations<?> toNumber();
  }
  
  MathOpetaionsBuilder math();
  
  
  //string
  List<String> asList(String ... values);
  
  //integer
  boolean lt(int arg0, int arg1);
  boolean lte(int arg0, int arg1);
  boolean gt(int arg0, int arg1);
  boolean gte(int arg0, int arg1);
  boolean eq(int arg0, int arg1);
  boolean neq(int arg0, int arg1);
  boolean between(int arg, int left, int right);
  
  //big decimal
  boolean lt(BigDecimal arg0, BigDecimal arg1);
  boolean lte(BigDecimal arg0, BigDecimal arg1);
  boolean gt(BigDecimal arg0, BigDecimal arg1);
  boolean gte(BigDecimal arg0, BigDecimal arg1);
  boolean eq(BigDecimal arg0, BigDecimal arg1);
  boolean neq(BigDecimal arg0, BigDecimal arg1);
  boolean between(BigDecimal arg, BigDecimal left, BigDecimal right);
  
  //date
  boolean lt(LocalDate arg0, LocalDate arg1);
  boolean lte(LocalDate arg0, LocalDate arg1);
  boolean gt(LocalDate arg0, LocalDate arg1);
  boolean gte(LocalDate arg0, LocalDate arg1);
  boolean eq(LocalDate arg0, LocalDate arg1);
  boolean neq(LocalDate arg0, LocalDate arg1);
  boolean between(LocalDate arg, LocalDate left, LocalDate right);
  
  //time
  boolean lt(LocalTime arg0, LocalTime arg1);
  boolean lte(LocalTime arg0, LocalTime arg1);
  boolean gt(LocalTime arg0, LocalTime arg1);
  boolean gte(LocalTime arg0, LocalTime arg1);
  boolean eq(LocalTime arg0, LocalTime arg1);
  boolean neq(LocalTime arg0, LocalTime arg1);
  boolean between(LocalTime arg, LocalTime left, LocalTime right);
  
  //date time
  boolean lt(LocalDateTime arg0, LocalDateTime arg1);
  boolean lte(LocalDateTime arg0, LocalDateTime arg1);
  boolean gt(LocalDateTime arg0, LocalDateTime arg1);
  boolean gte(LocalDateTime arg0, LocalDateTime arg1);
  boolean eq(LocalDateTime arg0, LocalDateTime arg1);
  boolean neq(LocalDateTime arg0, LocalDateTime arg1);
  boolean between(LocalDateTime arg, LocalDateTime left, LocalDateTime right);
}
