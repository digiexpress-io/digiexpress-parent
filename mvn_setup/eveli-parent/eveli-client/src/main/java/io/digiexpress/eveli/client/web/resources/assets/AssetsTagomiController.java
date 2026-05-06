package io.digiexpress.eveli.client.web.resources.assets;

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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.ModifyLocale.ModifyLocaleProps;
import io.resys.limaone.authoring.ModifyPrintout.ModifyPrintoutProps;
import io.resys.limaone.authoring.ModifyPrintoutPage.ModifyPrintoutPageProps;
import io.resys.limaone.authoring.ModifyPrintoutResource.ModifyPrintoutResourceProps;
import io.resys.limaone.authoring.NewLocale.NewLocaleProps;
import io.resys.limaone.authoring.NewPrintout.NewPrintoutProps;
import io.resys.limaone.authoring.NewPrintoutPage.NewPrintoutPageProps;
import io.resys.limaone.authoring.NewPrintoutResource.NewPrintoutResourceProps;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Printout;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.TagomiProgram;
import io.resys.limaone.program.TagomiProgram.PdfResult;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/worker/rest/api/assets/tagomi")
@Slf4j
@RequiredArgsConstructor
public class AssetsTagomiController {

  private final Authoring composer;
  private final Runtime runtime;

  @GetMapping("/")
  public Uni<ModelWorld> root() {
    return getSites();
  }
  @GetMapping("/sites")
  public Uni<ModelWorld> getSites() {
    return composer.worldQuery()
      .docs(
        BodyType.LOCALE,
        BodyType.PRINTOUT,
        BodyType.PRINTOUT_PAGE,
        BodyType.PRINTOUT_RESOURCE
      )
      .findAll();
  }

  @PostMapping("/resources")
  public Uni<Model<PrintoutResource>> createResource(@RequestBody NewPrintoutResourceProps body) {
    return composer.newModel().newPrintoutResource().props(body).build();
  }
  @PutMapping("/resources")
  public Uni<Model<PrintoutResource>> updateResource(@RequestBody ModifyPrintoutResourceProps body) {
    return composer.modifyModel().modifyPrintoutResource().props(body).build();
  }
  @DeleteMapping("/resources/{id}")
  public Uni<Model<?>> deleteResource(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(props -> props.bodyType(BodyType.PRINTOUT_RESOURCE).id(id)).build();
  }

  @PostMapping("/services")
  public Uni<Model<Printout>> createService(@RequestBody NewPrintoutProps body) {
    return composer.newModel().newPrintout().props(body).build();
  }
  @PutMapping("/services")
  public Uni<Model<Printout>> updateService(@RequestBody ModifyPrintoutProps body) {
    return composer.modifyModel().modifyPrintout().props(body).build();
  }
  @DeleteMapping("/services/{id}")
  public Uni<Model<Printout>> deleteService(@PathVariable("id") String id) {
    return composer.deleteModel().deletePrintout()
        .props(props -> props.printoutId(id)).build();
  }

  @PostMapping("/services/{id}/pdf/{locale}")
  public Uni<PdfResult> compilePdf(
      @PathVariable("id") String id,
      @PathVariable("locale") String locale,
      @RequestBody JsonNode props) {
    final TagomiProgram program = runtime.getBundle().queryTagomis().id(id).getOne();
    return program.run(locale, new JsonObject(props.toString()));
  }

  @PostMapping("/locales")
  public Uni<Model<Locale>> createLocale(@RequestBody NewLocaleProps body) {
    return composer.newModel().newLocale().props(body).build();
  }
  @PutMapping("/locales")
  public Uni<Model<Locale>> updateLocale(@RequestBody ModifyLocaleProps body) {
    return composer.modifyModel().modifyLocale().props(body).build();
  }
  @DeleteMapping("/locales/{id}")
  public Uni<Model<?>> deleteLocale(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(props -> props.bodyType(BodyType.LOCALE).id(id)).build();
  }

  @PostMapping("/templates")
  public Uni<Model<PrintoutPage>> createTemplate(@RequestBody NewPrintoutPageProps body) {
    return composer.newModel().newPrintoutPage().props(body).build();
  }
  @PutMapping("/templates")
  public Uni<Model<PrintoutPage>> updateTemplate(@RequestBody ModifyPrintoutPageProps body) {
    return composer.modifyModel().modifyPrintoutPage().props(body).build();
  }
  @DeleteMapping("/templates/{id}")
  public Uni<Model<PrintoutPage>> deleteTemplate(@PathVariable("id") String id) {
    return composer.deleteModel().deletePrintoutPage()
        .props(props -> props.printoutPageId(id)).build();
  }
}
