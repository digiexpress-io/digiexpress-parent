package io.resys.hdes.executor.spi.operations;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*-
 * #%L
 * hdes-compiler
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

public class HdesOperationsGen implements HdesOperations {
  private final static HdesOperationsGen IMPL = new HdesOperationsGen();
  
  public static HdesOperations get() {
    return IMPL;
  }
  
  //string
  @Override
  public List<String> asList(String ... values) {
    return Arrays.asList(values);
  }
  
  //integer
  @Override
  public boolean lt(int arg0, int arg1) {
    return arg0 < arg1;
  }
  @Override
  public boolean lte(int arg0, int arg1) {
    return arg0 <= arg1; 
  }
  @Override
  public boolean gt(int arg0, int arg1) {
    return arg0 > arg1;
  }
  @Override
  public boolean gte(int arg0, int arg1) {
    return arg0 >= arg1;
  }
  @Override
  public boolean eq(int arg0, int arg1) {
    return arg0 == arg1;
  }
  @Override
  public boolean neq(int arg0, int arg1) {
    return arg0 != arg1;
  }
  @Override
  public boolean between(int arg, int left, int right) {
    return left <= arg && right >= arg;
  }
  
  //big decimal
  @Override
  public boolean lt(BigDecimal arg0, BigDecimal arg1) {
    return arg0.compareTo(arg1) == -1;
  }
  @Override
  public boolean lte(BigDecimal arg0, BigDecimal arg1) {
    return arg0.compareTo(arg1) <= 0;    
  }
  @Override
  public boolean gt(BigDecimal arg0, BigDecimal arg1) {
    return arg0.compareTo(arg1) > -1;
  }
  @Override
  public boolean gte(BigDecimal arg0, BigDecimal arg1) {
    return arg0.compareTo(arg1) >= 0;
  }
  @Override
  public boolean eq(BigDecimal arg0, BigDecimal arg1) {
    return arg0.compareTo(arg1) == 0;
  }
  @Override
  public boolean neq(BigDecimal arg0, BigDecimal arg1) {
    return arg0.compareTo(arg1) != 0;
  }
  @Override
  public boolean between(BigDecimal arg, BigDecimal left, BigDecimal right) {
    return lte(left, arg) && gte(right, arg);
  }
  
  //date
  @Override
  public boolean lt(LocalDate arg0, LocalDate arg1) {
    return arg0.isBefore(arg1);
  }
  @Override
  public boolean lte(LocalDate arg0, LocalDate arg1) {
    return arg0.isBefore(arg1) || arg0.equals(arg1);
  }
  @Override
  public boolean gt(LocalDate arg0, LocalDate arg1) {
    return arg0.isAfter(arg1);
  }
  @Override
  public boolean gte(LocalDate arg0, LocalDate arg1) {
    return arg0.isAfter(arg1) || arg0.equals(arg1);
  }
  @Override
  public boolean eq(LocalDate arg0, LocalDate arg1) {
    return arg0.equals(arg1);
  }
  @Override
  public boolean neq(LocalDate arg0, LocalDate arg1) {
    return !arg0.equals(arg1);
  }
  @Override
  public boolean between(LocalDate arg, LocalDate left, LocalDate right) {
    return lte(left, arg) && gte(right, arg);
  }
  
  //time
  @Override
  public boolean lt(LocalTime arg0, LocalTime arg1) {
    return arg0.isBefore(arg1);
  }
  @Override
  public boolean lte(LocalTime arg0, LocalTime arg1) {
    return arg0.isBefore(arg1) || arg0.equals(arg1);
  }
  @Override
  public boolean gt(LocalTime arg0, LocalTime arg1) {
    return arg0.isAfter(arg1);
  }
  @Override
  public boolean gte(LocalTime arg0, LocalTime arg1) {
    return arg0.isAfter(arg1) || arg0.equals(arg1);
  }
  @Override
  public boolean eq(LocalTime arg0, LocalTime arg1) {
    return arg0.equals(arg1);
  }
  @Override
  public boolean neq(LocalTime arg0, LocalTime arg1) {
    return !arg0.equals(arg1);
  }
  @Override
  public boolean between(LocalTime arg, LocalTime left, LocalTime right) {
    return lte(left, arg) && gte(right, arg);
  }
  
  //date time
  @Override
  public boolean lt(LocalDateTime arg0, LocalDateTime arg1) {
    return arg0.isBefore(arg1);    
  }
  @Override
  public boolean lte(LocalDateTime arg0, LocalDateTime arg1) {
    return arg0.isBefore(arg1) || arg0.equals(arg1);
  }
  @Override
  public boolean gt(LocalDateTime arg0, LocalDateTime arg1) {
    return arg0.isAfter(arg1);
  }
  @Override
  public boolean gte(LocalDateTime arg0, LocalDateTime arg1) {
    return arg0.isAfter(arg1) || arg0.equals(arg1); 
  }
  @Override
  public boolean eq(LocalDateTime arg0, LocalDateTime arg1) {
    return arg0.equals(arg1);
  }
  @Override
  public boolean neq(LocalDateTime arg0, LocalDateTime arg1) {
    return !arg0.equals(arg1);
  }
  @Override
  public boolean between(LocalDateTime arg, LocalDateTime left, LocalDateTime right) {
    return lte(left, arg) && gte(right, arg);
  }

  @Override
  public MathOpetaionsBuilder math() {
    return new MathOpetaionsBuilder() {
      private final List<BigDecimal> decimals = new ArrayList<>();
      private final List<Integer> integers = new ArrayList<>();
      
      public MathOpetaionsBuilder decimal(BigDecimal ... values) {
        decimals.addAll(Arrays.asList(values));
        return this;
      }
      
      public MathOpetaionsBuilder decimal(Collection<BigDecimal> values) {
        decimals.addAll(values);
        return this;
      }
      public MathOpetaionsBuilder decimal(Optional<BigDecimal> value) {
        if(value.isPresent()) {
          decimals.add(value.get());
        }
        return this;
      }
      public MathOpetaionsBuilder integer(Collection<Integer> values) {
        integers.addAll(values);
        return this;
      }
      public MathOpetaionsBuilder integer(Optional<Integer> value) {
        if(value.isPresent()) {
          integers.add(value.get());
        }
        return this;
      }    
      public MathOpetaionsBuilder integer(Integer ... values) {
        integers.addAll(Arrays.asList(values));
        return this;
      }
      
      public MathOpetaionsBuilder any(Object any) {
        if(any instanceof Integer) {
          return integer((Integer) any);
        } else if(any instanceof BigDecimal) {
          return decimal((BigDecimal) any);
          
        } else if(any instanceof Collection) {
          for(Object value : ((Collection<?>) any)) {
            this.any((Serializable) value);
          }
          return this;
        } else if(any instanceof Optional) {
          if(((Optional<?>) any).isPresent()) {
            return this.any(((Optional<?>) any).get());  
          }
          return this;
        }
        throw new IllegalArgumentException("Expecting value to be Integer or BigDecimal but was: " + any); 
      }
      
      public DecimalOperations toDecimal() {
        List<BigDecimal> values = new ArrayList<>();
        decimals.stream().filter(d -> d != null).forEach(d -> values.add(d));
        integers.stream().filter(d -> d != null).forEach(d -> values.add(new BigDecimal(d)));      
        return new DecimalOperations(values);
      }
      public IntegerOperations toInteger() {
        List<Integer> values = new ArrayList<>();
        decimals.stream().filter(d -> d != null).forEach(d -> values.add(d.intValue()));
        integers.stream().filter(d -> d != null).forEach(d -> values.add(d));      
        return new IntegerOperations(values);
      }
      
      public MathOperations<?> toNumber() {
        if(decimals.isEmpty()) {
          return toInteger();
        }
        return toDecimal();
      }
    };
  }
  
  
  public static class DecimalOperations implements MathOperations<BigDecimal> {
    private final List<BigDecimal> values;
    public DecimalOperations(List<BigDecimal> values) {
      super();
      this.values = values;
    }
    
    public BigDecimal min() {
      return values.stream().min(Comparator.naturalOrder()).orElse(null);
    }
    public BigDecimal max() {
      return values.stream().max(Comparator.naturalOrder()).orElse(null);
    }    
    public BigDecimal sum() {
      return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal avg() {
      if(values.size() == 0) {
        return BigDecimal.ZERO;
      }
      BigDecimal sum = sum();
      if(sum.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      }
      return sum.divide(new BigDecimal(values.size()));
    }
  }

  public static class IntegerOperations implements MathOperations<Integer> {
    private final List<Integer> values;
    public IntegerOperations(List<Integer> values) {
      super();
      this.values = values;
    }
    
    public Integer min() {
      return values.stream().min(Comparator.naturalOrder()).orElse(null);
    }
    public Integer max() {
      return values.stream().max(Comparator.naturalOrder()).orElse(null);
    }    
    public Integer sum() {
      return values.stream().reduce(0, Integer::sum);
    }
    public BigDecimal avg() {
      if(values.size() == 0) {
        return BigDecimal.ZERO;
      }
      Integer sum = sum();
      if(sum == 0) {
        return BigDecimal.ZERO;
      }
      return new BigDecimal(sum).divide(new BigDecimal(values.size()));
    }
  }
}
