package io.resys.hdes.client.test;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import com.fasterxml.jackson.core.JsonProcessingException;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.client.test.config.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SummaryTest {

  @Test
  public void createTagSummaryTest() throws JsonProcessingException {
    final var tags = List.of(
        TestUtils.fileToStoreEntity("diff/v1.json", AstBody.AstBodyType.TAG),
        TestUtils.fileToStoreEntity("diff/v2.json", AstBody.AstBodyType.TAG)
    );

    final var v1Summary = TestUtils.client.summary()
        .tags(tags)
        .tagId("2eeb259a-d15e-4c7a-b1cb-ce18a3ef384b")
        .build();

    Assertions.assertEquals(
        FileUtils.toString(getClass(), "summary/summaryResult.json"),
        TestUtils.objectMapper.valueToTree(v1Summary).toString()
    );
  }
}
