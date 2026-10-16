package io.resys.thena.jackson;

/*-
 * #%L
 * thena-sql-client
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

import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.core.spi.JsonFactory;
import io.vertx.core.spi.json.JsonCodec;


// probably not the best of ideas
public class ThenaVertxJsonFactory implements JsonFactory {

  @Override
  public JsonCodec codec() {
      JsonCodec codec;
      try {
          // First try the Quarkus databind codec
          codec = new QuarkusJacksonJsonCodec();
      } catch (Throwable t1) {
          // Then try the Vert.x databind codec
          try {
              codec = new DatabindCodec();
          } catch (Throwable t2) {
              // Finally, use the jackson.core codec
              codec = new JacksonCodec();
          }
      }
      return codec;
  }
}
