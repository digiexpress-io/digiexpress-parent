package io.thestencil.client.spi.staticontent.visitors;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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


import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import io.thestencil.client.api.ImmutableHeading;
import io.thestencil.client.api.ImmutableImageTag;
import io.thestencil.client.api.Markdowns;



public class MarkdownVisitor {
  
  @Value.Immutable
  public interface MarkdownAst {
    List<Markdowns.ImageTag> getImages();
    List<Markdowns.Heading> getHeadings();    
  }
  
  private final List<Markdowns.ImageTag> images = new ArrayList<>();
  private final List<Markdowns.Heading> headings = new ArrayList<>();
  
  public MarkdownAst visit(String content) {
    final MutableDataSet options = new MutableDataSet();
    final Parser parser = Parser.builder(options).build();
    final Node document = parser.parse(content);
    this.visitDocument(document);    
    return ImmutableMarkdownAst.builder()
        .images(images)
        .headings(headings)
        .build();
  }
  
  private void visitDocument(Node node) {
    node.getChildren().forEach(next -> {
      this.visitNode(next);
      this.visitDocument(next);
    });
  }
  
  private void visitNode(Node node) {
    if(node instanceof Image) {
      Image img = (Image) node;
      images.add(ImmutableImageTag.builder()
          .line(img.getStartOfLine())
          .title(img.getTitle().toString())
          .altText(img.getText().toString())
          .path(img.getUrl().toString())
          .build());  
    } else if(node instanceof Heading) {
      Heading heading = (Heading) node;
      
      final var anchor = ImmutableHeading.builder()
        .order(headings.size() + 1)
        .level(heading.getLevel())
        .name(heading.getChars().toString())
        .build();
      headings.add(anchor);
    }
  }
}
