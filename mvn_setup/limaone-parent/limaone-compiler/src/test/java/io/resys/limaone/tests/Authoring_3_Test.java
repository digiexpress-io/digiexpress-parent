package io.resys.limaone.tests;

/*-
 * #%L
 * limaone-compiler
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_3_Test extends DbSupport {

  @Test
  public void tagomiTestWithPrintoutPageCreation() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    log.info("locale1: id={}, body={}", locale1.getId(), locale1.getBody());

    final var locale2 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("fi"))
        .buildSync();
    log.info("locale2: id={}, body={}", locale2.getId(), locale2.getBody());

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("application for loan")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("English printout name").build())
            .orchestratorName("wrench flow 1, we dont have it yet")
            .build()
        )
        .buildSync();
    log.info("printout1: id={}, body={}", printout1.getId(), printout1.getBody());
    Assertions.assertNotNull(printout1.getId());

    // create printout page for locale en
    final var printoutPage1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale1.getId())
            .content("= Application for loan\nThis is the English template content")
        )
        .buildSync();
    log.info("printoutPage1: id={}, body={}", printoutPage1.getId(), printoutPage1.getBody());
    Assertions.assertNotNull(printoutPage1.getId());

    // create printout page for locale fi
    final var printoutPage2 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale2.getId())
            .content("= Lainahakemus\nTämä on suomenkielinen mallipohja")
        )
        .buildSync();
    log.info("printoutPage2: id={}, body={}", printoutPage2.getId(), printoutPage2.getBody());
    Assertions.assertNotNull(printoutPage2.getId());

    {
      final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      log.info("worldState printoutPages size={}", worldState.getPrintoutPages().size());
      Assertions.assertEquals(2, worldState.getPrintoutPages().size());

      final var page1 = worldState.getPrintoutPages().get(printoutPage1.getId());
      log.info("worldState page1: body={}", page1 == null ? "null" : page1.getBody());
      Assertions.assertNotNull(page1);
      Assertions.assertEquals(printout1.getId(), page1.getBody().getServiceId());
      Assertions.assertEquals(locale1.getId(), page1.getBody().getLocaleId());
      Assertions.assertEquals("= Application for loan\nThis is the English template content", page1.getBody().getContent());

      final var page2 = worldState.getPrintoutPages().get(printoutPage2.getId());
      log.info("worldState page2: body={}", page2 == null ? "null" : page2.getBody());
      Assertions.assertNotNull(page2);
      Assertions.assertEquals(printout1.getId(), page2.getBody().getServiceId());
      Assertions.assertEquals(locale2.getId(), page2.getBody().getLocaleId());
    }

    // duplicate printout page for same locale + service should fail
    Assertions.assertThrows(Exception.class, () -> {
      authoring.newModel()
          .newPrintoutPage()
          .props(props -> props
              .serviceId(printout1.getId())
              .localeId(locale1.getId())
              .content("duplicate content")
          )
          .buildSync();
    });
  }
}
