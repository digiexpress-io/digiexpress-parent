/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.program.expr.arith;

import io.dialob.program.expr.CannotReduceTypeException;
import io.dialob.rule.parser.api.ValueType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;

public class Reducers {

  public static final OperatorTemplate<Integer> INTEGER_OPERATOR_TEMPLATE = new OperatorTemplate<Integer>() {
    @Override
    public Reducer<Integer> add() {
      return Int.ADD;
    }

    @Override
    public Reducer<Integer> sub() {
      return Int.SUB;
    }

    @Override
    public Reducer<Integer> mult() {
      return Int.MULT;
    }

    @Override
    public Reducer<Integer> div() {
      return Int.DIV;
    }
  };
  public static final OperatorTemplate<BigDecimal> DECIMAL_OPERATOR_TEMPLATE = new OperatorTemplate<BigDecimal>() {
    @Override
    public Reducer<BigDecimal> add() {
      return Number.ADD;
    }

    @Override
    public Reducer<BigDecimal> sub() {
      return Number.SUB;
    }

    @Override
    public Reducer<BigDecimal> mult() {
      return Number.MULT;
    }

    @Override
    public Reducer<BigDecimal> div() {
      return Number.DIV;
    }
  };
  public static final OperatorTemplate<Period> PERIOD_OPERATOR_TEMPLATE = new OperatorTemplate<Period>() {
    @Override
    public Reducer<Period> add() {
      return PeriodOps.ADD;
    }

    @Override
    public Reducer<Period> sub() {
      return PeriodOps.SUB;
    }

    @Override
    public Reducer<Period> mult() {
      throw new IllegalStateException("periods cannot be multiplied");
    }

    @Override
    public Reducer<Period> div() {
      throw new IllegalStateException("periods cannot be divided");
    }
  };
  public static final OperatorTemplate<Duration> DURATION_OPERATOR_TEMPLATE = new OperatorTemplate<Duration>() {
    @Override
    public Reducer<Duration> add() {
      return DurationOps.ADD;
    }

    @Override
    public Reducer<Duration> sub() {
      return DurationOps.SUB;
    }

    @Override
    public Reducer<Duration> mult() {
      throw new CannotReduceTypeException("CANNOT_MULTIPLY", ValueType.DURATION);
    }

    @Override
    public Reducer<Duration> div() {
      throw new CannotReduceTypeException("CANNOT_DIVIDE", ValueType.DURATION);
    }
  };

  public interface OperatorTemplate<T> {
    Reducer<T> add();

    Reducer<T> sub();

    Reducer<T> mult();

    Reducer<T> div();
  }

  @Nonnull
  public static OperatorTemplate<?> ofType(@Nonnull ValueType valueType) {
    if (valueType == ValueType.DECIMAL) {
      return DECIMAL_OPERATOR_TEMPLATE;
    }
    if (valueType == ValueType.INTEGER) {
      return INTEGER_OPERATOR_TEMPLATE;
    }
    if (valueType == ValueType.PERIOD) {
      return PERIOD_OPERATOR_TEMPLATE;
    }
    if (valueType == ValueType.DURATION) {
      return DURATION_OPERATOR_TEMPLATE;
    }
    throw new CannotReduceTypeException("NO_ARITMETHIC_FOR_TYPE", valueType);
  }


  public enum PeriodOps implements Reducer<Period> {
    ADD {
      @Override
      public Period reduce(Period period, Period t2) {
        return period.plus(t2).normalized();
      }
    },
    SUB {
      @Override
      public Period reduce(Period period, Period t2) {
        return period.minus(t2).normalized();
      }
    };

    @Nonnull
    @Override
    public ValueType getValueType() {
      return ValueType.PERIOD;
    }
  }

  public enum DurationOps implements Reducer<Duration> {
    ADD {
      @Override
      public Duration reduce(Duration duration, Duration t2) {
        return duration.plus(t2);
      }
    },
    SUB {
      @Override
      public Duration reduce(Duration duration, Duration t2) {
        return duration.minus(t2);
      }
    };

    @Nonnull
    @Override
    public ValueType getValueType() {
      return ValueType.DURATION;
    }
  }

  public enum Number implements Reducer<BigDecimal> {
    ADD {
      @Override
      public BigDecimal reduce(BigDecimal a, BigDecimal t2) {
        return a.add(t2);
      }
    },
    SUB {
      @Override
      public BigDecimal reduce(BigDecimal a, BigDecimal t2) {
        return a.subtract(t2);
      }
    },
    DIV {
      @Override
      public BigDecimal reduce(BigDecimal a, BigDecimal t2) {
        return a.divide(t2);
      }
    },
    MULT {
      @Override
      public BigDecimal reduce(BigDecimal a, BigDecimal t2) {
        return a.multiply(t2);
      }
    };

    @Nonnull
    @Override
    public ValueType getValueType() {
      return ValueType.DECIMAL;
    }
  }

  public enum Int implements Reducer<Integer> {
    ADD {
      @Override
      public Integer reduce(Integer i, Integer t2) {
        return i + t2;
      }
    },
    SUB {
      @Override
      public Integer reduce(Integer i, Integer t2) {
        return i - t2;
      }
    },
    DIV {
      @Override
      public Integer reduce(Integer i, Integer t2) {
        return i / t2;
      }
    },
    MULT {
      @Override
      public Integer reduce(Integer i, Integer t2) {
        return i * t2;
      }
    };

    @Nonnull
    @Override
    public ValueType getValueType() {
      return ValueType.INTEGER;
    }
  }

  public enum Bool implements Reducer<Boolean> {
    AND {
      @Override
      public Boolean reduce(Boolean i, Boolean t2) {
        return i && t2;
      }
    },
    OR {
      @Override
      public Boolean reduce(Boolean i, Boolean t2) {
        return i || t2;
      }
    };

    @Nonnull
    @Override
    public ValueType getValueType() {
      return ValueType.BOOLEAN;
    }
  }

  public enum Str implements Reducer<String> {
    CAT {
      @Override
      public String reduce(String i, String t2) {
        return i + t2;
      }
    };

    @Nonnull
    @Override
    public ValueType getValueType() {
      return ValueType.STRING;
    }
  }

}
