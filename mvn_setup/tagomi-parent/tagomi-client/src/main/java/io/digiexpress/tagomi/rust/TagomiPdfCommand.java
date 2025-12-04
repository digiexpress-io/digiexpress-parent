package io.digiexpress.tagomi.rust;

/*-
 * #%L
 * tagomi-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.tagomi.rust.entities.AnyResponse;
import io.digiexpress.tagomi.rust.entities.PdfRequest;
import io.digiexpress.tagomi.rust.entities.PdfDocument;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiPdfCommand {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

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
        final var base64Pdf = pdfResponse.getData().getBase64();
        //return Base64.getDecoder().decode(base64Pdf);
        return base64Pdf;
        
      } else {
        throw new PdfCompilationException(
            "PDF compilation failed: " + pdfResponse.getError()
            );
      }
    } catch (HttpClientErrorException.BadRequest e) {
      try {
        AnyResponse<PdfDocument> errorResponse = objectMapper.readValue(
            e.getResponseBodyAsString(),
            new TypeReference<AnyResponse<PdfDocument>>() {}
            );
        throw new PdfCompilationException(
            "PDF compilation failed: " + errorResponse.getError()
            );
      } catch (Exception parseEx) {
        throw new PdfCompilationException(
            "PDF compilation failed with error: " + e.getResponseBodyAsString(),
            parseEx
            );
      }
    } catch (Exception e) {
      throw new PdfCompilationException(
          "Failed to communicate with PDF service",
          e
          );
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
