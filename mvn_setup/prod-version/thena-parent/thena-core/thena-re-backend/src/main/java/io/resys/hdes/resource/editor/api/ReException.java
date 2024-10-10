package io.resys.hdes.resource.editor.api;

import java.util.function.Supplier;

/*-
 * #%L
 * hdes-pm-repo
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

import org.immutables.value.Value;


public class ReException extends RuntimeException {
  private static final long serialVersionUID = 208954132433481316L;

  private final ConstraintViolation value;
  
  public static enum ConstraintType {
    NOT_FOUND, NOT_UNIQUE, INVALID_DATA
  }
  
  public static enum ErrorType {
    
  }
  
  public ReException(ConstraintViolation value, Supplier<String> msg, Throwable cause) {
    super(msg.get(), cause);
    this.value = value;
  }

  public ReException(ConstraintViolation value, Supplier<String> msg) {
    super(msg.get());
    this.value = value;
  }
  
  public ConstraintViolation getValue() {
    return value;
  }
  
  
  @Value.Immutable
  public interface ConstraintViolation {
    String getId();
    String getRev();
    ConstraintType getConstraint();
    ErrorType getType();
  }
}
