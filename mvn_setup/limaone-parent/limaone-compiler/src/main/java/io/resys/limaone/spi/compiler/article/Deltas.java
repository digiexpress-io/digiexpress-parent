package io.resys.limaone.spi.compiler.article;

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
import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import com.google.common.collect.ComparisonChain;
import com.google.common.hash.Hashing;

import io.resys.limaone.ast.Article_AST;
import io.resys.limaone.ast.Article_AST.ImageTag;
import io.resys.limaone.program.ArticleProgram.TopicBlob;
import io.resys.limaone.program.ArticleProgram.TopicHeading;
import io.resys.limaone.program.ImmutableTopicBlob;
import io.resys.limaone.program.ImmutableTopicHeading;

public interface Deltas {

  
  @Value.Immutable
  interface TopicData {
    String getPath();
    String getLocale();
    String getValue();
    Boolean getAuth();
    List<Article_AST.Heading> getHeadings();
    List<ImageTag> getImages();
    
    default String getFullPath() {
      return getPath() + "/" + getValue();
    }
    
    default List<TopicHeading> getTopicHeadings() {
      List<TopicHeading> result = new ArrayList<>();
      int index = 1;
      for(final var heading : this.getHeadings()) {      
        result.add(ImmutableTopicHeading.builder()
          .id(String.valueOf(index++))
          .name(heading.getName())
          .order(heading.getOrder())
          .level(heading.getLevel())
          .build());
      }
      result.sort((a, b) -> ComparisonChain.start()
          .compare(a.getLevel(), b.getLevel())
          .compare(a.getName(), b.getName())
          .result());
      return result;
    }
    
    
    
    default TopicBlob getTopicBlob() {
      final String blob = this.getValue();
      final var id = Hashing.murmur3_128().hashString(blob, StandardCharsets.UTF_8).toString();
      return ImmutableTopicBlob.builder().id(id).value(blob).build();      
    }

    default String getParentTopic() {
      final String[] path = this.getPath().split("\\/");
      if(path.length > 1) {
        return path[0];
      }
      return null;
    }
    default String getTopicName() {
      for(final var heading : this.getHeadings()) {
        if(heading.getLevel() == 1 && heading.getName().length() > 1) {
          return heading.getName().substring(1).trim();
        }
      }
      return this.getPath();
    } 
  }
}
