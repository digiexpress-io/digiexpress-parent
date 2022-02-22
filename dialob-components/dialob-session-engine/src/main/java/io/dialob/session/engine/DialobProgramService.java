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
package io.dialob.session.engine;

import io.dialob.session.engine.program.DialobProgram;

import javax.annotation.Nonnull;

public interface DialobProgramService {

  @Nonnull
  DialobProgram findByFormId(@Nonnull String formId);

  @Nonnull
  DialobProgram findByFormIdAndRev(@Nonnull String formId, String formRev);

}
