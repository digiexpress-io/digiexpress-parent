package io.resys.thena.fs.spi.commit;

/*-
 * #%L
 * thena-fs-client
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

import io.vertx.core.json.JsonObject;

public class CommitBuilderException extends RuntimeException {

  private static final long serialVersionUID = 3868491498774789368L;

  public CommitBuilderException(Exception e, String msg, JsonObject props) {
    super(msg + System.lineSeparator() + props.encodePrettily(), e);
  }
  public CommitBuilderException(Exception e, JsonObject props) {
    super(props.encodePrettily(), e);
  }
  
  public CommitBuilderException(String msg, JsonObject props) {
    super(msg + System.lineSeparator() + props.encodePrettily());
  }
}
