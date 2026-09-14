package io.digiexpress.eveli.client.test.metis.search;

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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.digiexpress.eveli.client.web.resources.gamut.GamutSiteSearchController;
import io.resys.metis.search.api.ImmutableMetisSearchResult;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchClient.SearchQuery;
import io.resys.metis.search.api.MetisSearchResult;
import io.smallrye.mutiny.TimeoutException;
import io.smallrye.mutiny.Uni;
import jakarta.servlet.http.HttpServletRequest;

public class GamutSiteSearchControllerTest {

  @Test
  void overTheRateLimitFallsBackWithoutCallingSearch() {
    final var search = Mockito.mock(MetisSearchClient.class);
    final var query = query(search, Uni.createFrom().item(List.of(hit())));
    Mockito.when(search.isIndexReadyForPortal()).thenReturn(true);

    final var props = new EveliPropsMetisSearch();
    props.getQuery().setRateLimitRequests(1);
    final var controller = new GamutSiteSearchController(search, props);
    final var request = request("203.0.113.10");

    final var allowed = controller.findByText("kirjasto", "fi", 8, request).await().indefinitely();
    Assertions.assertFalse(allowed.getFallback());
    Assertions.assertEquals(1, allowed.getResults().size());

    final var limited = controller.findByText("kirjasto", "fi", 8, request).await().indefinitely();
    Assertions.assertTrue(limited.getFallback());
    Assertions.assertTrue(limited.getResults().isEmpty());
    Mockito.verify(query, Mockito.times(1)).findByText("kirjasto");
  }

  @Test
  void aSearchTimeoutFallsBackToKeywordSearch() {
    final var search = Mockito.mock(MetisSearchClient.class);
    query(search, Uni.createFrom().failure(new TimeoutException()));
    Mockito.when(search.isIndexReadyForPortal()).thenReturn(true);

    final var response = controller(search).findByText("kirjasto", "fi", 8, request("203.0.113.11"))
        .await().indefinitely();

    Assertions.assertTrue(response.getFallback());
    Assertions.assertTrue(response.getResults().isEmpty());
  }

  @Test
  void aSearchErrorFallsBackToKeywordSearch() {
    final var search = Mockito.mock(MetisSearchClient.class);
    query(search, Uni.createFrom().failure(new IllegalStateException("embedding queue full")));
    Mockito.when(search.isIndexReadyForPortal()).thenReturn(true);

    final var response = controller(search).findByText("kirjasto", "fi", 8, request("203.0.113.12"))
        .await().indefinitely();

    Assertions.assertTrue(response.getFallback());
    Assertions.assertTrue(response.getResults().isEmpty());
  }

  @Test
  void anIndexThatIsNotReadyFallsBackWithoutCallingSearch() {
    final var search = Mockito.mock(MetisSearchClient.class);
    Mockito.when(search.isIndexReadyForPortal()).thenReturn(false);

    final var response = controller(search).findByText("kirjasto", "fi", 8, request("203.0.113.14"))
        .await().indefinitely();

    Assertions.assertTrue(response.getFallback());
    Assertions.assertTrue(response.getResults().isEmpty());
    Mockito.verify(search, Mockito.never()).query();
  }

  @Test
  void aFailedIndexStatusCheckFallsBackWithoutCallingSearch() {
    final var search = Mockito.mock(MetisSearchClient.class);
    Mockito.when(search.isIndexReadyForPortal()).thenThrow(new IllegalStateException("db is down"));

    final var response = controller(search).findByText("kirjasto", "fi", 8, request("203.0.113.13"))
        .await().indefinitely();

    Assertions.assertTrue(response.getFallback());
    Assertions.assertTrue(response.getResults().isEmpty());
    Mockito.verify(search, Mockito.never()).query();
  }

  private static GamutSiteSearchController controller(MetisSearchClient search) {
    final var props = new EveliPropsMetisSearch();
    props.getQuery().setRateLimitRequests(0);
    return new GamutSiteSearchController(search, props);
  }

  private static SearchQuery query(MetisSearchClient search, Uni<List<MetisSearchResult>> results) {
    final var query = Mockito.mock(SearchQuery.class);
    Mockito.when(search.query()).thenReturn(query);
    Mockito.when(query.locale(Mockito.anyString())).thenReturn(query);
    Mockito.when(query.limit(Mockito.anyInt())).thenReturn(query);
    Mockito.when(query.findByText(Mockito.anyString())).thenReturn(results);
    return query;
  }

  private static HttpServletRequest request(String remoteAddr) {
    final var request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
    return request;
  }

  private static MetisSearchResult hit() {
    return ImmutableMetisSearchResult.builder()
        .workflowId("wf-library")
        .title("Varaa kirja kirjastosta")
        .locale("fi")
        .score(1.0)
        .build();
  }
}
