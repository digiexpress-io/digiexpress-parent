package io.resys.limaone.spi.pdf;

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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.resys.limaone.spi.pdf.entities.AnyResponse;
import io.resys.limaone.spi.pdf.entities.PdfDocument;
import io.resys.limaone.spi.pdf.entities.PdfRequest;

public class TagomiPdfCommand {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public TagomiPdfCommand(ObjectMapper objectMapper, String baseUrl) {
    this.objectMapper = objectMapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    this.restTemplate = new RestTemplate(List.of(
        new ByteArrayHttpMessageConverter(),
        new StringHttpMessageConverter(),
        new MappingJackson2HttpMessageConverter(this.objectMapper)
    ));
    this.baseUrl = baseUrl;
  }

  public String compilePdf(PdfRequest request) {
    final var url = baseUrl + "/compile";

    final var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    final var entity = new HttpEntity<PdfRequest>(request, headers);

    try {
      final var response = restTemplate.postForEntity(url, entity, String.class);
      final var pdfResponse = objectMapper.readValue(
          response.getBody(),
          new TypeReference<AnyResponse<PdfDocument>>() {}
      );

      if (Boolean.TRUE.equals(pdfResponse.getSuccess()) && pdfResponse.getData() != null) {
        return pdfResponse.getData().getBase64();
      } else {
        throw new PdfCompilationException("PDF compilation failed: " + pdfResponse.getError());
      }
    } catch (HttpClientErrorException.BadRequest e) {
      try {
        final var errorResponse = objectMapper.readValue(
            e.getResponseBodyAsString(),
            new TypeReference<AnyResponse<PdfDocument>>() {}
        );
        throw new PdfCompilationException("PDF compilation failed: " + errorResponse.getError());
      } catch (PdfCompilationException rethrow) {
        throw rethrow;
      } catch (Exception parseEx) {
        throw new PdfCompilationException("PDF compilation failed with error: " + e.getResponseBodyAsString(), parseEx);
      }
    } catch (PdfCompilationException rethrow) {
      throw rethrow;
    } catch (Exception e) {
      throw new PdfCompilationException("Failed to communicate with PDF service", e);
    }
  }

  public static class PdfCompilationException extends RuntimeException {
    private static final long serialVersionUID = -1320398072484853093L;

    public PdfCompilationException(String message) {
      super(message);
    }

    public PdfCompilationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
