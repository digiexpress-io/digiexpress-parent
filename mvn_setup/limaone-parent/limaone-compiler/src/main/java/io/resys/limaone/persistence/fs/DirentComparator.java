package io.resys.limaone.persistence.fs;

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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.resys.limaone.fs.WorldFs.DirentBase;
import io.resys.limaone.fs.WorldFsProps;
import io.resys.limaone.model.Model.BodyType;

public class DirentComparator implements Comparator<DirentBase> {

  private static final Map<BodyType, Integer> TYPE_ORDER = Map.of(
    BodyType.ARTICLE,          0,
    BodyType.FOLDER,           1,
    BodyType.ARTICLE_PAGE,     2,
    BodyType.ARTICLE_LINK,     3,
    BodyType.ARTICLE_WORKFLOW, 4,
    BodyType.FLOW_TASK,        5
  );

  @Override
  public int compare(DirentBase o1, DirentBase o2) {
    final int p1 = TYPE_ORDER.getOrDefault(o1.getType(), 99);
    final int p2 = TYPE_ORDER.getOrDefault(o2.getType(), 99);

    if (p1 != p2) {
      return Integer.compare(p1, p2);
    }

    return switch (o1.getType()) {
      case FOLDER -> {
        final var articleProps1 = findArticleProps(o1.getChildren());
        final var articleProps2 = findArticleProps(o2.getChildren());
        final boolean isPages1 = articleProps1 == null && hasArticlePageChildren(o1.getChildren());
        final boolean isPages2 = articleProps2 == null && hasArticlePageChildren(o2.getChildren());
        if (isPages1 && !isPages2) { yield -1; }
        if (!isPages1 && isPages2) { yield 1; }
        if (articleProps1 == null && articleProps2 == null) {
          yield o1.getName().compareToIgnoreCase(o2.getName());
        }
        final int order1 = articleProps1 != null && articleProps1.getOrderNumber() != null ? articleProps1.getOrderNumber() : Integer.MAX_VALUE;
        final int order2 = articleProps2 != null && articleProps2.getOrderNumber() != null ? articleProps2.getOrderNumber() : Integer.MAX_VALUE;
        yield Integer.compare(order1, order2);
      }
      case ARTICLE -> {
        final var props1 = (WorldFsProps.ArticleProps) o1.getProps();
        final var props2 = (WorldFsProps.ArticleProps) o2.getProps();
        final int order1 = props1 != null ? props1.getOrderNumber() : 0;
        final int order2 = props2 != null ? props2.getOrderNumber() : 0;
        yield Integer.compare(order1, order2);
      }
      case ARTICLE_PAGE, FLOW_TASK, ARTICLE_WORKFLOW, FLOW, DECISION_TABLE, LOCALE -> o1.getName().compareToIgnoreCase(o2.getName());
      default -> 0;
    };
  }

  private static WorldFsProps.ArticleProps findArticleProps(List<DirentBase> children) {
    if (children == null) {
      return null;
    }
    return children.stream()
      .filter(c -> c.getType() == BodyType.ARTICLE)
      .findFirst()
      .map(c -> (WorldFsProps.ArticleProps) c.getProps())
      .orElse(null);
  }

  private static boolean hasArticlePageChildren(List<DirentBase> children) {
    if (children == null) {
      return false;
    }
    return children.stream().anyMatch(c -> c.getType() == BodyType.ARTICLE_PAGE);
  }
}
