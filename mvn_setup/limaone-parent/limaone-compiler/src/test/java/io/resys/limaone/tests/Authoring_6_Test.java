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
public class Authoring_6_Test extends DbSupport {

  @Test
  public void deletePrintoutResource_removesResource() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("loan application")
            .orchestratorName("flow-1")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("English").build())
            .build()
        )
        .buildSync();
    log.info("printout1: id={}, body={}", printout1.getId(), printout1.getBody());

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale1.getId())
            .content("= Loan application")
        )
        .buildSync();
    log.info("page1: id={}, body={}", page1.getId(), page1.getBody());

    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props
            .resourceName("header.typ")
            .contentType("text/*")
            .uploadBody("#let header = () => [Header]")
            .printoutPageIds(List.of())
        )
        .buildSync();
    log.info("resource1: id={}, body={}", resource1.getId(), resource1.getBody());

    final var deleted = authoring.deleteModel()
        .deleteAny()
        .props(props -> props.id(resource1.getId()).bodyType(BodyType.PRINTOUT_RESOURCE))
        .buildSync();
    log.info("deleted resource: id={}, body={}", deleted.getId(), deleted.getBody());

    Assertions.assertEquals(resource1.getId(), deleted.getId());

    final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(0, world.getPrintoutResources().size());
    Assertions.assertEquals(1, world.getPrintoutPages().size());
    Assertions.assertEquals(1, world.getPrintouts().size());
  }

  @Test
  public void deletePrintoutPage_removesPage() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();
    log.info("page1: id={}, body={}", page1.getId(), page1.getBody());

    final var deleted = authoring.deleteModel()
        .deleteAny()
        .props(props -> props.id(page1.getId()).bodyType(BodyType.PRINTOUT_PAGE))
        .buildSync();
    log.info("deleted page: id={}, body={}", deleted.getId(), deleted.getBody());

    Assertions.assertEquals(page1.getId(), deleted.getId());

    final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(0, world.getPrintoutPages().size());
    Assertions.assertEquals(1, world.getPrintouts().size());
  }

  @Test
  public void deletePrintoutPage_usedByResource_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    authoring.newModel()
        .newPrintoutResource()
        .props(props -> props
            .resourceName("script.typ")
            .contentType("text/*")
            .uploadBody("#let h = () => []")
            .printoutPageIds(List.of(page1.getId()))
        )
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.deleteModel()
          .deleteAny()
          .props(props -> props.id(page1.getId()).bodyType(BodyType.PRINTOUT_PAGE))
          .buildSync()
    );
  }

  @Test
  public void deletePrintout_removesPrintout() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("to be deleted")
            .orchestratorName("orch")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();
    log.info("printout1: id={}, body={}", printout1.getId(), printout1.getBody());

    final var deleted = authoring.deleteModel()
        .deleteAny()
        .props(props -> props.id(printout1.getId()).bodyType(BodyType.PRINTOUT))
        .buildSync();
    log.info("deleted printout: id={}, body={}", deleted.getId(), deleted.getBody());

    Assertions.assertEquals(printout1.getId(), deleted.getId());

    final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(0, world.getPrintouts().size());
  }

  @Test
  public void deletePrintout_hasPages_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.deleteModel()
          .deleteAny()
          .props(props -> props.id(printout1.getId()).bodyType(BodyType.PRINTOUT))
          .buildSync()
    );
  }

  @Test
  public void deletePrintout_fullLifecycle_createAndDelete() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();
    log.info("locale1: id={}", locale1.getId());

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("loan application")
            .orchestratorName("flow-1")
            .addLocaleLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("Loan").build())
            .build()
        )
        .buildSync();
    log.info("printout1: id={}", printout1.getId());

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale1.getId())
            .content("= Loan application")
        )
        .buildSync();
    log.info("page1: id={}", page1.getId());

    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props
            .resourceName("header.typ")
            .contentType("text/*")
            .uploadBody("#let h = () => [Header]")
            .printoutPageIds(List.of(page1.getId()))
        )
        .buildSync();
    log.info("resource1: id={}", resource1.getId());

    {
      final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      Assertions.assertEquals(1, world.getPrintouts().size());
      Assertions.assertEquals(1, world.getPrintoutPages().size());
      Assertions.assertEquals(1, world.getPrintoutResources().size());
    }

    // delete resource first (it references the page)
    authoring.deleteModel()
        .deleteAny()
        .props(props -> props.id(resource1.getId()).bodyType(BodyType.PRINTOUT_RESOURCE))
        .buildSync();
    log.info("deleted resource: id={}", resource1.getId());

    // delete page (no resources reference it anymore)
    authoring.deleteModel()
        .deleteAny()
        .props(props -> props.id(page1.getId()).bodyType(BodyType.PRINTOUT_PAGE))
        .buildSync();
    log.info("deleted page: id={}", page1.getId());

    // delete printout (no pages remain)
    authoring.deleteModel()
        .deleteAny()
        .props(props -> props.id(printout1.getId()).bodyType(BodyType.PRINTOUT))
        .buildSync();
    log.info("deleted printout: id={}", printout1.getId());

    final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(0, world.getPrintouts().size());
    Assertions.assertEquals(0, world.getPrintoutPages().size());
    Assertions.assertEquals(0, world.getPrintoutResources().size());
  }
}
