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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.api.form.Form;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.program.TagomiProgram.PdfStatus;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.pdf.TagomiPdfRendererImpl;
import io.resys.limaone.tests.support.DbSupport;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.resys.thena.test.TagomiTest;
import io.resys.thena.test.TagomiTest.TagomiUrl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TagomiTest(enabled = true)
@DialobTest(enabled = true)
public class TagomiPdfTest extends DbSupport {

  @Test
  public void compilePdfFromTypstTemplate(TagomiUrl tagomiUrl) {
    final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    final var renderer = new TagomiPdfRendererImpl(objectMapper, tagomiUrl.getTagomiUrl());
    final var config = createConfig(null, renderer);
    final var authoring = new AuthoringImpl(config);

    final var locale = authoring.newModel().newLocale()
        .props(p -> p.locale("en")).buildSync();

    final var printout = authoring.newModel().newPrintout()
        .props(p -> p
            .serviceName("test-doc")
            .orchestratorName("")
            .addLocaleLabels(ImmutableLocaleLabel.builder()
                .locale(locale.getId()).labelValue("Test").build()))
        .buildSync();

    authoring.newModel().newPrintoutPage()
        .props(p -> p
            .serviceId(printout.getId())
            .localeId(locale.getId())
            .content("= Hello World\nThis is a test PDF."))
        .buildSync();

    final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    final var compiler = new CompilerImpl(config.getEnvir());
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var program = runtime.getBundle().queryTagomis().name("test-doc").getOne();

    final var result = program.run("en", new JsonObject()).await().atMost(Duration.ofMinutes(1));

    Assertions.assertEquals(PdfStatus.OK, result.getStatus(),
        "PDF compilation failed: " + result.getStatusMessage());
    Assertions.assertNotNull(result.getBodyBase64(), "PDF body should not be null");

    final var pdfBytes = Base64.getDecoder().decode(result.getBodyBase64());
    Assertions.assertEquals('%', pdfBytes[0], "PDF magic byte 0");
    Assertions.assertEquals('P', pdfBytes[1], "PDF magic byte 1");
    Assertions.assertEquals('D', pdfBytes[2], "PDF magic byte 2");
    Assertions.assertEquals('F', pdfBytes[3], "PDF magic byte 3");
  }

  @Test
  @DialobResetDB
  public void renderPrintoutWithDialobAnswersAndLinkedTemplate(FormUrl formUrl, TagomiUrl tagomiUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);
    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);

    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));
    formDb.withTenant().mergeForm()
        .props(created).build()
        .await().atMost(Duration.ofMinutes(1));

    final var sessionId = formDb.withTenant().createFormInstance()
        .formId(created.getId()).build()
        .await().atMost(Duration.ofMinutes(1));

    formDb.withTenant().createFormFill()
        .formInstanceId(sessionId.getId())
        .actions(JsonObject.of(
            "rev", sessionId.getRev(),
            "actions", new JsonArray(
                """
                    [
                      {'type':'ANSWER','answer':'no','id':'authentication'},
                      {'type':'ANSWER','answer':'cityService','id':'mainList'},
                      {'type':'ANSWER','answer':'info','id':'cityServiceMainList'},
                      {'type':'ANSWER','answer':'thanks','id':'typeOfFeedback'},
                      {'type':'ANSWER','answer':'thank you','id':'feedBackTitle'},
                      {'type':'ANSWER','answer':'very big text','id':'feedBackTxt'},
                      {'type':'ANSWER','answer':false,'id':'boolean11'},
                      {'type':'COMPLETE'}
                    ]""".replace("'", "\"")))
            .encode())
        .build()
        .await().atMost(Duration.ofMinutes(1));

    final var completed = formDb.withTenant().formInstanceQuery().getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1));
    log.info("completed questionnaire status: {}", completed.getQuestionnaire().getMetadata().getStatus());

    final var props = new JsonObject();
    final var persistedAnswers = completed.getQuestionnaire().getAnswers();
    if (persistedAnswers != null) {
      for (final var answer : persistedAnswers) {
        final var value = answer.getValue() != null ? answer.getValue() : answer.acceptedValue();
        if (value != null) {
          props.put(answer.getId(), value);
        }
      }
    }
    log.info("dialob answers as props: {}", props.encode());
    Assertions.assertFalse(props.isEmpty(), "expected at least one filled Dialob answer to flow into props");

    final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    final var renderer = new TagomiPdfRendererImpl(objectMapper, tagomiUrl.getTagomiUrl());
    final var config = createConfig(null, renderer);
    final var authoring = new AuthoringImpl(config);
    final var locale = authoring.newModel().newLocale()
        .props(p -> p.locale("en")).buildSync();

    final var printout = authoring.newModel().newPrintout()
        .props(p -> p
            .serviceName("feedback-doc")
            .orchestratorName("")
            .addLocaleLabels(ImmutableLocaleLabel.builder()
                .locale(locale.getId()).labelValue("Feedback").build()))
        .buildSync();

    final var appendixPrintout = authoring.newModel().newPrintout()
        .props(p -> p
            .serviceName("appendix")
            .orchestratorName("")
            .addLocaleLabels(ImmutableLocaleLabel.builder()
                .locale(locale.getId()).labelValue("Appendix").build()))
        .buildSync();

    final var page1 = authoring.newModel().newPrintoutPage()
        .props(p -> p
            .serviceId(printout.getId())
            .localeId(locale.getId())
            .content(
                """
                    = Feedback report

                    Main list selection: #sys.inputs.service.props.mainList
                    """))
        .buildSync();
    log.info("page1 (initial): id={}", page1.getId());

    final var page2 = authoring.newModel().newPrintoutPage()
        .props(p -> p
            .serviceId(appendixPrintout.getId())
            .localeId(locale.getId())
            .content(
                """
                    == Linked appendix page

                    This page is linked to the initial printout via printoutPageIds.
                    """))
        .buildSync();
    log.info("page2 (new template): id={}", page2.getId());

    // text resource attached to the new template
    final var resource = authoring.newModel().newPrintoutResource()
        .props(p -> p
            .resourceName("appendix.typ")
            .contentType("text/*")
            .uploadBody(
                """
                    #let appendix_note() = [
                      Submitted via integration test.
                    ]
                    """)
            .printoutPageIds(List.of(page2.getId())))
        .buildSync();
    log.info("resource (text/*): id={}, name={}", resource.getId(), resource.getBody().getResourceName());
    authoring.modifyModel().modifyPrintoutPage()
        .props(p -> p.pageId(page1.getId()).printoutPageIds(List.of(page2.getId())))
        .buildSync();

    final var world = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    final var compiler = new CompilerImpl(config.getEnvir());
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var program = runtime.getBundle().queryTagomis().name("feedback-doc").getOne();
    final var result = program.run("en", props).await().atMost(Duration.ofMinutes(1));

    Assertions.assertEquals(PdfStatus.OK, result.getStatus(),
        "PDF compilation failed: " + result.getStatusMessage());
    Assertions.assertNotNull(result.getBodyBase64(), "PDF body should not be null");

    final var pdfBytes = Base64.getDecoder().decode(result.getBodyBase64());
    Assertions.assertEquals('%', pdfBytes[0], "PDF magic byte 0");
    Assertions.assertEquals('P', pdfBytes[1], "PDF magic byte 1");
    Assertions.assertEquals('D', pdfBytes[2], "PDF magic byte 2");
    Assertions.assertEquals('F', pdfBytes[3], "PDF magic byte 3");

    Assertions.assertTrue(pdfBytes.length > 1000,
        "PDF should have content, got " + pdfBytes.length + " bytes");

    final var pdfString = new String(pdfBytes, StandardCharsets.ISO_8859_1);
    Assertions.assertTrue(pdfString.contains("stream"),
        "PDF should contain content stream");
  }
}
