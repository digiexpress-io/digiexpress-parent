package io.resys.limaone.spi.program.input;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ArticleProgram.ArticleProgramInput;
import io.resys.limaone.program.ProgramInput;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultArticleProgramInput implements ArticleProgramInput {
  private static final long serialVersionUID = 8677063201201611855L;
  private final ProgramInput delegate;
  private final String locale;
  private final OffsetDateTime targetDate;
  private final Participant user;
  
  @Override
  public Serializable getValue(Parameter parameter) {
    return delegate.getValue(parameter);
  }
  @Override
  public ResolvedParameter getValueWithMeta(String name) {
    return delegate.getValueWithMeta(name);
  }
  @Override
  public ProgramInput withInputs(Map<String, Serializable> nextInputs) {
    return new DefaultArticleProgramInput(delegate.withInputs(nextInputs), locale, targetDate, user);
  }
  @Override
  public OffsetDateTime getTargetDate() {
    return targetDate;
  }
  @Override
  public Participant getParticipant() {
    return user;
  }
  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private Participant user;
    private String locale;
    
    private OffsetDateTime targetDate;
    private io.resys.limaone.program.Runtime runtime;
    private Map<String, Serializable> inputs;
    
    public Builder runtime(io.resys.limaone.program.Runtime runtime) {this.runtime = Objects.requireNonNull(runtime, () -> "runtime must be defined"); return this;}
    public Builder locale(String locale) {this.locale = Objects.requireNonNull(locale, () -> "locale must be defined"); return this;}
    public Builder user(Participant user) {this.user = Objects.requireNonNull(user, () -> "user must be defined"); return this;}
    public Builder targetDate(OffsetDateTime targetDate) {this.targetDate = Objects.requireNonNull(targetDate, () -> "targetDate must be defined"); return this;}
    
    
    public DefaultArticleProgramInput build() {

      Objects.requireNonNull(user, () -> "user must be defined");
      Objects.requireNonNull(locale, () -> "locale must be defined");
      Objects.requireNonNull(targetDate, () -> "targetDate must be defined");
      Objects.requireNonNull(runtime, () -> "runtime must be defined");
      
      final var input = DefaultProgramInput.of(Optional.ofNullable(this.inputs).orElse(Collections.emptyMap()));
      return new DefaultArticleProgramInput(input, locale, targetDate, user);
    }
  }
}
