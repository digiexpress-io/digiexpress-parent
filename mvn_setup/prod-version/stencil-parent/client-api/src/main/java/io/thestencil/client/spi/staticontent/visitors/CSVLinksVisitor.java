package io.thestencil.client.spi.staticontent.visitors;

/*-
 * #%L
 * stencil-static-content
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import io.thestencil.client.api.ImmutableLinkResource;
import io.thestencil.client.api.Markdowns.LinkResource;

public class CSVLinksVisitor {
  private String file;

  public CSVLinksVisitor(String file) {
    super();
    this.file = file;
  }
  
  public List<LinkResource> visit(byte[] input) {
    final Reader reader = new InputStreamReader(new ByteArrayInputStream(input), StandardCharsets.UTF_8);
    CSVParser parser = null;
    try {
      parser = new CSVParser(reader, CSVFormat.EXCEL
          .withHeader()
          .withCommentMarker('/')
          .withAllowMissingColumnNames(false)
          .withIgnoreEmptyLines(true)
          );
      
      return visitParser(parser); 
    } catch(Exception e) {
      throw new MarkdownException("Failed to parse links file: '" + file + "', error: " + e.getMessage(), e);
    } finally {
      try {
        if(parser != null) {
          parser.close();          
        }
        reader.close();
      } catch(IOException e) {
        throw new MarkdownException("Failed to parse links file: '" + file + "', error: " + e.getMessage(), e);
      }
    }
  }
  
  private List<LinkResource> visitParser(CSVParser parser) {
    final List<LinkResource> result = new ArrayList<>();
    for (final CSVRecord record : parser) {
      result.add(visitRecord(record));
    }
    return result;
  }
  
  private LinkResource visitRecord(CSVRecord record) {
    
    final var path = record.get("path");
    final var value = record.get("value");
    final var type = visitType(record);
    
    return ImmutableLinkResource.builder()
        .id(file +"_row_" + record.getRecordNumber())
        .locale(visitLocale(record))
        .desc(record.get("description"))
        .path(path)
        .value(value)
        .type(type)
        .workflow(SiteStateVisitor.LINK_TYPE_WORKFLOW.equals(type))
        .global(path == null || path.isBlank())
        .build();
  }
  
  private String visitType(CSVRecord record) {
    try {
      final var type = record.get("type");
      return type;
    } catch(Exception e) {
      throw new MarkdownException("Failed to parse links"
          + " file: '" + file + "', "
          + " row: '" + record.getRecordNumber() + "',"
          + " column: 'type',"
          + " error: " + e.getMessage(), e);
    }
  }

  private List<String> visitLocale(CSVRecord record) {
    final var locale = record.get("locale");
    if(locale.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.asList(locale.split("\\/"));
  }
}
