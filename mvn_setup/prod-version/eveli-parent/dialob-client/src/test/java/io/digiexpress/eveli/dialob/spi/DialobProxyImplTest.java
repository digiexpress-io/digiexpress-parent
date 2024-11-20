package io.digiexpress.eveli.dialob.spi;

/*-
 * #%L
 * dialob-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DialobProxyImplTest {

  @Test
  void testSessionIdValidation() {
    Assertions.assertTrue(DialobProxyImpl.invalidSessionId(null));
    Assertions.assertTrue(DialobProxyImpl.invalidSessionId(""));
    Assertions.assertTrue(DialobProxyImpl.invalidSessionId("x"));

    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("a"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("A"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("0"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("1"));
    Assertions.assertFalse(DialobProxyImpl.invalidSessionId("0123456789aBcDeF-123"));
  }
  
}
