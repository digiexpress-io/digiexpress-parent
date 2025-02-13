package io.resys.hdes.client.api.ast;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableAstService.class)
@JsonDeserialize(as = ImmutableAstService.class)
public interface AstService extends AstBody, Serializable {

  @JsonIgnore
  Class<? extends ServiceExecutorType> getBeanType();
  String getValue();
  @Nullable
  TypeDef getTypeDef0();
  @Nullable
  TypeDef getTypeDef1();
  @Nullable
  TypeDef getReturnDef1();
  
  List<AstServiceRef> getRefs();
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableAstServiceRef.class)
  @JsonDeserialize(as = ImmutableAstServiceRef.class)
  interface AstServiceRef extends Serializable {
    AstBodyType getBodyType();
    String getRefValue();
  }
  
  AstServiceType getExecutorType();
  enum AstServiceType { TYPE_0, TYPE_1, TYPE_2 }
  
  interface ServiceExecutorType {}
  
  interface ServiceExecutorType0<O  extends Serializable> extends ServiceExecutorType {
    O execute();
  }  
  interface ServiceExecutorType1<I, O extends Serializable> extends ServiceExecutorType {
    O execute(I input1);
  }
  interface ServiceExecutorType2<I, I2, O extends Serializable> extends ServiceExecutorType {
    O execute(I input1, I2 input2);
  }
}
