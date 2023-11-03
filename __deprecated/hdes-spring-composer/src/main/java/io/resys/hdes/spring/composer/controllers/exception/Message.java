package io.resys.hdes.spring.composer.controllers.exception;

/*-
 * #%L
 * wrench-component-messages
 * %%
 * Copyright (C) 2016 Copyright 2016 ReSys OÜ
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
import java.text.MessageFormat;

public class Message implements Serializable {

  private static final long serialVersionUID = -1526741610475665283L;

  private String code;
  private String logCode;
  private String value;
  private String context;
  private Object[] args;

  public Message() {
  }

  public Message(String code, String value, Object[] args) {
    super();
    this.code = code;
    this.value = value;
    this.context = null;
    this.args = args;
  }

  public Message(String code, String value) {
    super();
    this.code = code;
    this.value = value;
    this.context = null;
  }

  public Message(String code, String value, String context) {
    super();
    this.code = code;
    this.value = value;
    this.context = context;
  }

  public Message(String code, String value, String context, Object[] args) {
    super();
    this.code = code;
    this.value = value;
    this.context = context;
    this.args = args;
  }

  public String getCode() {
    return code;
  }

  public String getValue() {
    return value;
  }

  public String getContext() {
    return context;
  }

  public Message setCode(String id) {
    this.code = id;
    return this;
  }

  public Message setValue(String value) {
    this.value = value;
    return this;
  }

  public Message setContext(String context) {
    this.context = context;
    return this;
  }

  @Override
  public String toString() {
    final String msg;
    if(args == null) {
      msg = value == null ?  "": value.toString();
    } else {
      msg = MessageFormat.format(value, args);
    }
    return  "code: " + code + ", " + msg;
  }

  public Object[] getArgs() {
    return args;
  }

  public Message setArgs(Object[] args) {
    this.args = args;
    return this;
  }

  public String getLogCode() {
    return logCode;
  }

  public Message setLogCode(String logCode) {
    this.logCode = logCode;
    return this;
  }
}
