package io.digiexpress.eveli.client.test.metis.search;

/*-
 * #%L
 * eveli-client
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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.digiexpress.eveli.client.spi.metis.search.MetisLiveIndexTrigger;
import io.digiexpress.eveli.client.spi.metis.search.MetisLivePublicationReconciler;

public class MetisLivePublicationReconcilerTest {

  @Test
  void checkLivePublicationDelegatesWhenReindexOnDeploymentIsOn() {
    final var trigger = Mockito.mock(MetisLiveIndexTrigger.class);
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(true);

    new MetisLivePublicationReconciler(trigger, props).checkLivePublication();

    Mockito.verify(trigger).startIfLivePublicationChanged();
  }

  @Test
  void checkLivePublicationIsANoOpWhenReindexOnDeploymentIsOff() {
    final var trigger = Mockito.mock(MetisLiveIndexTrigger.class);
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(false);

    new MetisLivePublicationReconciler(trigger, props).checkLivePublication();

    Mockito.verifyNoInteractions(trigger);
  }
}
