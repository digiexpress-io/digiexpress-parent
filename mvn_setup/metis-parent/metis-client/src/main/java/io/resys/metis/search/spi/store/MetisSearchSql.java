package io.resys.metis.search.spi.store;

/*-
 * #%L
 * metis-client
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

import java.time.Duration;

import io.resys.metis.search.spi.store.spi.MetisSearchDbImpl;
import io.resys.thena.storesql.PgErrors;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

public final class MetisSearchSql {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  private MetisSearchSql() {}

  public static MetisSearchDb create(Pool pgPool) {
    return MetisSearchDbImpl.create()
        .client(pgPool)
        .errorHandler(new PgErrors())
        .build();
  }

  public static void truncate(MetisSearchDb db) {
    await(db.query().queryMetisSearchIndex().deleteAll(true));
    await(db.query().queryMetisSearchReindexJob().deleteAll(true));
  }

  public static <T> T await(Uni<T> uni) {
    return uni.await().atMost(TIMEOUT);
  }

  public static String toVectorLiteral(float[] values) {
    if (values == null) {
      return null;
    }
    final var text = new StringBuilder(values.length * 8).append('[');
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        text.append(',');
      }
      text.append(values[i]);
    }
    return text.append(']').toString();
  }

  public static boolean isDuplicate(Throwable error) {
    final var handler = new PgErrors();
    for (var cause = error; cause != null; cause = cause.getCause()) {
      if (handler.duplicate(cause)) {
        return true;
      }
    }
    return false;
  }
}
