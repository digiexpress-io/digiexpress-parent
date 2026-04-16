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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_4_Test extends DbSupport {

  @Test
  public void tagomiTestWithPrintoutResourceCreation() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    log.info("locale1: id={}, body={}", locale1.getId(), locale1.getBody());

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("application for loan")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("English printout name").build())
            .orchestratorName("wrench flow 1, we dont have it yet")
            .build()
        )
        .buildSync();
    log.info("printout1: id={}, body={}", printout1.getId(), printout1.getBody());

    final var printoutPage1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale1.getId())
            .content("= Application for loan\nThis is the English template content")
        )
        .buildSync();
    log.info("printoutPage1: id={}, body={}", printoutPage1.getId(), printoutPage1.getBody());

    // create a script resource linked to the printout page
    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props
            .resourceName("header-script.typ")
            .contentType("text/*")
            .uploadBody("#let header = () => [== Header]")
            .templateIds(List.of(printoutPage1.getId()))
        )
        .buildSync();
    log.info("resource1: id={}, body={}", resource1.getId(), resource1.getBody());
    Assertions.assertNotNull(resource1.getId());

    // create a script resource with no template links
    final var resource2 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props
            .resourceName("footer-script.typ")
            .contentType("text/*")
            .uploadBody("#let footer = () => [== Footer]")
            .templateIds(List.of())
        )
        .buildSync();
    log.info("resource2: id={}, body={}", resource2.getId(), resource2.getBody());
    Assertions.assertNotNull(resource2.getId());

    {
      final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      log.info("worldState printoutResources size={}", worldState.getPrintoutResources().size());
      Assertions.assertEquals(2, worldState.getPrintoutResources().size());

      final var r1 = worldState.getPrintoutResources().get(resource1.getId());
      Assertions.assertNotNull(r1);
      Assertions.assertEquals("header-script.typ", r1.getBody().getResourceName());
      Assertions.assertEquals("text/*", r1.getBody().getContentType());
      Assertions.assertEquals("#let header = () => [== Header]", r1.getBody().getContent());
      Assertions.assertEquals(List.of(printoutPage1.getId()), r1.getBody().getTemplateIds());

      final var r2 = worldState.getPrintoutResources().get(resource2.getId());
      Assertions.assertNotNull(r2);
      Assertions.assertEquals("footer-script.typ", r2.getBody().getResourceName());
    }

    // duplicate resource name should fail
    Assertions.assertThrows(Exception.class, () -> {
      authoring.newModel()
          .newPrintoutResource()
          .props(props -> props
              .resourceName("header-script.typ")
              .contentType("text/*")
              .uploadBody("some content")
              .templateIds(List.of())
          )
          .buildSync();
    });

    // invalid templateId should fail
    Assertions.assertThrows(Exception.class, () -> {
      authoring.newModel()
          .newPrintoutResource()
          .props(props -> props
              .resourceName("invalid-template-resource.typ")
              .contentType("text/*")
              .uploadBody("some content")
              .templateIds(List.of("non-existent-template-id"))
          )
          .buildSync();
    });
  }
}
