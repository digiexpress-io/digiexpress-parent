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
public class Authoring_5_Test extends DbSupport {

  @Test
  public void modifyPrintout_updatesServiceNameAndOrchestrator() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("original service name")
            .orchestratorName("original orchestrator")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("English label").build())
            .build()
        )
        .buildSync();
    log.info("printout1: id={}, body={}", printout1.getId(), printout1.getBody());
    Assertions.assertNotNull(printout1.getId());

    final var updated = authoring.modifyModel()
        .modifyPrintout()
        .props(props -> props
            .serviceId(printout1.getId())
            .serviceName("updated service name")
            .orchestratorName("updated orchestrator"))
        .buildSync();
    log.info("updated printout: id={}, body={}", updated.getId(), updated.getBody());

    Assertions.assertEquals("updated service name", updated.getBody().getServiceName());
    Assertions.assertEquals("updated orchestrator", updated.getBody().getOrchestratorName());

    final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(1, worldState.getPrintouts().size());
    final var body = worldState.getPrintouts().get(printout1.getId()).getBody();
    Assertions.assertEquals("updated service name", body.getServiceName());
    Assertions.assertEquals("updated orchestrator", body.getOrchestratorName());
  }

  @Test
  public void modifyPrintout_updatesLabels() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();
    final var locale2 = authoring.newModel()
        .newLocale().props(props -> props.locale("fi")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("loan application")
            .orchestratorName("flow-1")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("English label").build())
            .build()
        )
        .buildSync();

    final var updated = authoring.modifyModel()
        .modifyPrintout()
        .props(props -> props
            .serviceId(printout1.getId())
            .serviceName("loan application")
            .orchestratorName("flow-1")
            .labels(List.of(
                ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("Updated English").build(),
                ImmutableLocaleLabel.builder().locale(locale2.getId()).labelValue("Finnish label").build()
            )))
        .buildSync();

    Assertions.assertEquals(2, updated.getBody().getLabels().size());
  }

  @Test
  public void modifyPrintout_invalidLocaleInLabels_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("en label").build())
            .build()
        )
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.modifyModel()
          .modifyPrintout()
          .props(props -> props
              .serviceId(printout1.getId())
              .serviceName("service")
              .orchestratorName("orch")
              .labels(List.of(
                  ImmutableLocaleLabel.builder().locale("non-existent-locale-id").labelValue("bad label").build()
              )))
          .buildSync()
    );
  }

  @Test
  public void modifyPrintoutResource_updatesNameAndContent() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale1.getId())
            .content("= Original content")
        )
        .buildSync();

    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props
            .resourceName("original-script.typ")
            .contentType("text/*")
            .uploadBody("#let header = () => [Original]")
            .templateIds(List.of(page1.getId()))
        )
        .buildSync();
    log.info("resource1: id={}, body={}", resource1.getId(), resource1.getBody());

    final var updated = authoring.modifyModel()
        .modifyPrintoutResource()
        .props(props -> props
            .resourceId(resource1.getId())
            .resourceName("updated-script.typ")
            .uploadBody("#let header = () => [Updated]"))
        .buildSync();
    log.info("updated resource: id={}, body={}", updated.getId(), updated.getBody());

    Assertions.assertEquals("updated-script.typ", updated.getBody().getResourceName());
    Assertions.assertEquals("#let header = () => [Updated]", updated.getBody().getContent());
    // templateIds preserved when not specified
    Assertions.assertEquals(List.of(page1.getId()), updated.getBody().getTemplateIds());
  }

  @Test
  public void modifyPrintoutResource_duplicateName_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props.resourceName("script-a.typ").contentType("text/*").uploadBody("body a").templateIds(List.of(page1.getId())))
        .buildSync();

    authoring.newModel()
        .newPrintoutResource()
        .props(props -> props.resourceName("script-b.typ").contentType("text/*").uploadBody("body b").templateIds(List.of()))
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.modifyModel()
          .modifyPrintoutResource()
          .props(props -> props.resourceId(resource1.getId()).resourceName("script-b.typ"))
          .buildSync()
    );
  }

  @Test
  public void modifyPrintoutResource_invalidTemplateId_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props.resourceName("script.typ").contentType("text/*").uploadBody("body").templateIds(List.of(page1.getId())))
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.modifyModel()
          .modifyPrintoutResource()
          .props(props -> props.resourceId(resource1.getId()).templateIds(List.of("non-existent-page-id")))
          .buildSync()
    );
  }

  @Test
  public void modifyPrintoutPage_updatesContent() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props
            .serviceId(printout1.getId())
            .localeId(locale1.getId())
            .content("= Original content")
        )
        .buildSync();
    log.info("page1: id={}, body={}", page1.getId(), page1.getBody());

    final var updated = authoring.modifyModel()
        .modifyPrintoutPage()
        .props(props -> props.pageId(page1.getId()).content("= Updated content"))
        .buildSync();
    log.info("updated page: id={}, body={}", updated.getId(), updated.getBody());

    Assertions.assertEquals("= Updated content", updated.getBody().getContent());
    Assertions.assertEquals(locale1.getId(), updated.getBody().getLocaleId());
    Assertions.assertEquals(printout1.getId(), updated.getBody().getServiceId());
  }

  @Test
  public void modifyPrintoutPage_updatesLocale() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();
    final var locale2 = authoring.newModel()
        .newLocale().props(props -> props.locale("fi")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("English content"))
        .buildSync();

    final var updated = authoring.modifyModel()
        .modifyPrintoutPage()
        .props(props -> props.pageId(page1.getId()).localeId(locale2.getId()))
        .buildSync();

    Assertions.assertEquals(locale2.getId(), updated.getBody().getLocaleId());
  }

  @Test
  public void modifyPrintoutPage_managesResourceLinks_bidirectional() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    final var resource1 = authoring.newModel()
        .newPrintoutResource()
        .props(props -> props.resourceName("script.typ").contentType("text/*").uploadBody("body").templateIds(List.of()))
        .buildSync();

    // link resource to page via modifyPrintoutPage
    authoring.modifyModel()
        .modifyPrintoutPage()
        .props(props -> props.pageId(page1.getId()).resourceIds(List.of(resource1.getId())))
        .buildSync();

    {
      final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      final var r = world.getPrintoutResources().get(resource1.getId());
      Assertions.assertTrue(r.getBody().getTemplateIds().contains(page1.getId()),
          "resource should have page1 in its templateIds after linking");
    }

    // unlink resource from page
    authoring.modifyModel()
        .modifyPrintoutPage()
        .props(props -> props.pageId(page1.getId()).resourceIds(List.of()))
        .buildSync();

    {
      final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      final var r = world.getPrintoutResources().get(resource1.getId());
      Assertions.assertFalse(r.getBody().getTemplateIds().contains(page1.getId()),
          "resource should NOT have page1 in its templateIds after unlinking");
    }
  }

  @Test
  public void modifyPrintoutPage_invalidLocale_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.modifyModel()
          .modifyPrintoutPage()
          .props(props -> props.pageId(page1.getId()).localeId("non-existent-locale-id"))
          .buildSync()
    );
  }

  @Test
  public void modifyPrintoutPage_invalidTemplateDependency_throws() {
    final var authoring = new AuthoringImpl(createConfig());

    final var locale1 = authoring.newModel()
        .newLocale().props(props -> props.locale("en")).buildSync();

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("service")
            .orchestratorName("orch")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("label").build())
            .build()
        )
        .buildSync();

    final var page1 = authoring.newModel()
        .newPrintoutPage()
        .props(props -> props.serviceId(printout1.getId()).localeId(locale1.getId()).content("content"))
        .buildSync();

    Assertions.assertThrows(Exception.class, () ->
      authoring.modifyModel()
          .modifyPrintoutPage()
          .props(props -> props.pageId(page1.getId()).templateIds(List.of("non-existent-page-id")))
          .buildSync()
    );
  }
}
