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
package io.dialob.client.tests.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.dialob.api.proto.Action;
import io.dialob.client.tests.steps.support.AbstractWebSocketTests;


public class QuestionnaireWebSocketTest extends AbstractWebSocketTests {

  @Test
  public void shouldGetQUESTIONNAIRE_NOT_FOUNDActionIfAskedQuestionnaireDoNotExists2() throws Exception {
    openSession("q-1")
      .expectActions(actions -> {
        assertEquals(1, actions.getActions().size());
        assertNull(actions.getActions().get(0).getTrace());
        assertEquals(Action.Type.SERVER_ERROR, actions.getActions().get(0).getType());
        assertEquals("not found", actions.getActions().get(0).getMessage());
        assertEquals("q-1", actions.getActions().get(0).getId());
      })
      .execute();
  }

  @Test
  public void connectWebsocketPerQuestionnaireID() throws Exception {

    openSession("1234")
      .expectActions(actions -> {
        assertEquals(1, actions.getActions().size());
        assertNull(actions.getActions().get(0).getTrace());
        assertEquals(Action.Type.SERVER_ERROR, actions.getActions().get(0).getType());
        assertEquals("1234", actions.getActions().get(0).getId());
      })
      .execute();
  }
}
