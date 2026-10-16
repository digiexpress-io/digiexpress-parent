# Metis semantic site search: how it works

Enablement (flags, Flyway, Ollama including registry pull / air-gap, GCloud,
Kubernetes replica lock) is in the
[Metis README](../../README.md#first-deployment). Properties are in
[docs/README_CONFIG_PROPERTIES.md](../../../../docs/README_CONFIG_PROPERTIES.md).

Citizens search in Gamut. Staff use Eveli. Authors publish a limaone **bundle**
(pages, topics, workflow links) as a **publication**, which can go live
immediately or at a future `liveDate`. Metis is the Digiexpress AI platform;
semantic site search is the first capability on it.

The portal already filtered the site JSON in the browser: form titles, topic
names, phones, hyperlinks. Semantic site search replaces the **form and topic**
lists when the backend returns a real ranking. Phones and hyperlinks stay on
that client-side filter. The point is queries in everyday language that do not
appear in the form title — keyword search cannot do that, vector search can
if the indexed metadata actually contains those words.

Each indexed row has two representations: PostgreSQL `tsvector` (stemmed
keyword search for `fi` / `en` / `sv`) and a `bge-m3` embedding in pgvector
(1024 dimensions, cosine). At query time the two ranked lists are fused with
reciprocal rank fusion. If the backend is not ready, Gamut keeps the keyword
search it already had. The citizen never sees a search error.

Code split:

- `io.resys.metis.search` in `metis-client` — documents, index, ranking, job
- `io.resys.metis.spi.ai` — `EmbeddingService`, `StructuredChatService`
- `eveli-client` — Spring, REST, Flyway, permissions, live-publication trigger
- `gamut-api` — `useBackendSearch`, `backend-results`
- `eveli-primitives/eveli-metis` — worker status / reindex page


## Search path

The box in the public header is `GPopoverSearch`. The signed-in services page
uses the same backend through `SearchResults`. After the citizen stops typing
for 350 ms, `useBackendSearch` calls
`GET /portal/site/search?q=…&locale=fi`.

That endpoint is public because `/portal/site/**` already is — the same as the
site JSON.

Two clocks:

- The **server** gives hybrid search 5 s (`query.timeout-seconds`). On timeout
  or any unexpected error it returns `{ fallback: true, results: [] }` and
  Gamut stays on keyword search for that query.
- The **browser** aborts the HTTP call 8 s after the request is sent (350 ms
  debounce + 8 s). The extra time is so a 5 s server success still lands. If
  the connection hangs with no body, keyword search takes over at ~8 s.

If the latest reindex job is not `COMPLETED` (including never having run), the
controller does not call hybrid search at all. The response is
`{ fallback: true, results: [] }` and the portal stays on keyword search.
Otherwise hybrid search runs: full-text search (FTS), embed the query, nearest
neighbours in the vector index, then fuse the lists. Hits carry `workflowId`
and `topicId`; the portal maps those ids onto the site JSON it already has.
Display names are not a join key.

`q` is truncated at 400 characters. `limit` defaults to 8 and cannot exceed 8
(`default-limit` and `max-results` should stay equal, or callers silently get
fewer rows than they asked for). Locale comparison is case-insensitive; a
Finnish query only sees Finnish rows.

```json
{
  "query": "I need a library card",
  "locale": "en",
  "fallback": false,
  "results": [
    {
      "workflowId": "…",
      "topicId": "…",
      "title": "Library card application",
      "category": "Library",
      "score": 0.0131,
      "vectorScore": 0.72,
      "ftsScore": 0.41
    }
  ]
}
```

`score` is the fused rank score, not cosine similarity. `vectorScore` /
`ftsScore` are the raw list contributions when that side had the hit.

### When Gamut falls back to keyword search

`fallback: true` (or a 404 that marks the endpoint `unavailable`) means
“ignore this body, use the browser filter”. That happens when:

- no completed job (`NONE`, empty table, `RUNNING`, `CANCELLING`, `FAILED`,
  `CANCELLED`)
- the per-IP rate limit is exceeded (below)
- the search thread pool is full (`concurrency` 4, `queue-capacity` 50)
- the 5 s server timeout, or any unexpected error
- every hit fails to map onto the live site JSON

A slow or rejected **query embedding** is not this path. Hybrid still ranks
with PostgreSQL FTS and `fallback` stays `false`, so the citizen still gets
server-side keyword ranking rather than the cruder browser substring filter.

`FAILED` / `CANCELLED` remain visible on
`GET /worker/rest/api/metis/search/status` so someone can re-run reindex. The
portal does not wait on them.

### Rate limiting

Hybrid search may call Ollama. The portal endpoint is unauthenticated, so a
single client can otherwise pin the embedding process. The controller
therefore counts requests **per client IP** and, past the cap, skips hybrid
search.

Defaults (`eveli.metis.search.query.rate-limit-requests` /
`rate-limit-window-seconds`): **10 requests in 10 seconds**. The 11th from
the same address in that window gets `{ fallback: true, results: [] }` without
calling ranking. Set `rate-limit-requests` to `0` to turn the limiter off
(tests do this).

This limiter is not a result cache. Repeating the same `q` still runs search
until the cap, except that **query embeddings** of the exact same string are
cached separately (see Ranking).

### Frontend

`useBackendSearch` fetches. `useSemanticResults` maps ids onto the site JSON.
While the request is `pending` (including the 350 ms debounce), form and
topic lists render empty so keyword matches do not flash then get replaced.
Phones and hyperlinks are also hidden until the answer lands.

A `CircularProgress` is shown only after the HTTP call has been in flight for
250 ms. Warm answers (~30 ms after debounce) therefore do not flicker a
spinner. Closing the public popover keeps the last query and its results; the
input is bound to that state so reopen shows both.

How the fetch is classified:

- `unavailable` (404/401/403): keyword for the rest of the session — the
  controller is not there or not allowed
- `fallback: true`, non-OK, or network error: keyword for this query
- some ids missing: keep the mapped ones, log the rest
- every id missing: keyword (index and live site disagree)
- empty array with `fallback: false`: no-results, not a fallback

The same workflow can sit on more than one topic. The lookup prefers a
`searchOnly` topic when one exists, which is the synthetic search topic the
portal already uses for “open form”.

## What gets indexed

One row per workflow link per locale, unique on `(workflow_id, locale)`. A
result is a **form**, not a topic and not an article. Topics only contribute
text (and a `topic_id` so the portal can highlight the page). Phones,
hyperlinks, and internal pages are not documents. A workflow with no owning
topic is skipped.

Limaone workflow links barely have metadata (name, form name, flow name), so
`SiteSearchDocumentBuilder` pulls from every topic that links to that
workflow.

PostgreSQL full-text search weights bands **A–D**. A match in a higher band
ranks above a match in a lower one (title beats page body).

| Source | Where it goes |
| --- | --- |
| Workflow name | `title`, FTS **A** |
| Form name + flow name | `description`, FTS **B** |
| Owning topic names | first is `category`; all of them go into supplemental and the LLM context |
| Headings, page markdown (stripped, max 800 chars), sibling link titles | supplemental, FTS **D** |
| Chat-model helpers | `ai_metadata`, FTS **C**, also appended to the embedding input |

A topic with more than `generic-topic-threshold` workflows (default 4, so five
or more) is treated as a listing page. Body, headings and sibling titles are
dropped so an “all services” page does not end up in every document. The topic
title is still the category and still goes to the metadata model.

The indexer does not re-apply draft / disabled / auth filters. It indexes
whatever the article program emits for an anonymous participant
(`metis-indexer`) at `OffsetDateTime.now()` — the same view a logged-out
citizen gets. limaone already drops `disabled`, `inHouse`, and `devMode`
(unless the compiler is in dev). Those never reach `LocalizedSite`.

Login-required services **are** indexed. The article program still lists
`topic.auth` in the anonymous JSON; the UI hides “open form” via
`isFormLinkEnabled`. Search can find them, opening the form is still gated.
Hiding them from search but not from the tree would make the two surfaces
disagree.

## Reading the live bundle

`SiteContentReader` is the only limaone call at index time. It asks limaone
for the compiled **bundle** (the published article program plus workflows)
and runs that program once per configured locale (`en, fi, sv` by default).
A missing locale is logged and skipped. If none resolve, the job fails with
`ContentUnavailableException` and does not touch index rows.

Limaone caches compiled bundles so it does not recompile on every call:

- `getBundle()` is the normal view, held for about 30 seconds.
- `getCachelessBundle()` is a Runtime-level view held for about 5 seconds,
  shared with other callers.

The indexer must see a publication that just went live, not a bundle compiled
before that. It therefore calls `getCachelessBundle().withCacheless()`, which
builds a **new** lazy bundle with a ~2 s debounce instead of reusing the
5-second Runtime object. Without that extra `withCacheless()`, a reindex
started on the deploy event can still read the previous HEAD.

The limaone world hash of that view is stored on the job as `bundle_hash`. A
completed job only counts as “this publication is indexed” while that hash
still matches cacheless HEAD, so a read that still saw the previous bundle
cannot skip the new one forever.

There has to be a live publication (`startsAt` null or not in the future).
Among live ones, latest `startsAt` wins, then `createdAt`. Authoring import
does not fire a deploy event. A future `liveDate` is indexed when it becomes
live, not when the record is created. Immediate publishes start a job on the
event.

## Metadata, embeddings, hash

For each document, `llama3.2` (locally) is asked for a short description,
synonyms, and related phrases in that locale. Prompt is in `MetadataGenerator`.
The result is `ai_metadata`: concatenated onto the embedding input, and FTS
band C. Those phrases are what let a query like “I need a library card” match
a form whose title is a proper name. The LLM only sees title, description,
and topic names (truncated); page body is already in `searchText`. Two failed
chat attempts fall back to description-only metadata. A missing description
does not fail the document.

Bump `indexing.metadata-prompt-version` when the prompt changes. It is part of
the content hash, so a normal reindex rewrites every row.

Embeddings are `bge-m3`, cosine similarity, column `VECTOR(1024)`.
`indexing.embedding-dimension` only **checks** that width. A mismatch fails
the job. Changing width is a new Flyway migration.

Index-time `embed()` is not gated by the query-embed semaphore; indexing
concurrency (default 1) already limits how many documents run. The 600 s
document timeout starts when that document starts, not when it was queued.

Content hash is SHA-256 of locale, owning topic id, title, search text,
embedding model id, and the prompt version. Unchanged hashes are skipped on
the next reindex. Switching the embedding model or bumping the prompt version
invalidates them. `force=true` ignores hashes. `ai_metadata` is not in the
hash — the hash is computed before the chat call, and the prompt version
stands in for “helpers might have changed”.

## Store

Flyway `classpath:db/postgresql` always includes `V4_1__metis_search.sql`. Semantic site search
owns **V4_x**; later capabilities take V5_x+ under the same location. The script uses only
built-in types so vanilla PostgreSQL still migrates. When `eveli.metis.search.enabled` is true,
boot runs an idempotent ensure: `CREATE EXTENSION vector`, `pg_trgm`, `unaccent`, then
`embedding VECTOR(1024)` plus HNSW and trigram indexes. The app role needs `CREATE EXTENSION`,
or a DBA pre-creates them (typical on Cloud SQL).

`metis_search_index`: unique `(workflow_id, locale)`; GIN on `search_vector`
(full-text); HNSW on `embedding` (the usual pgvector index for cosine
nearest-neighbour, added when search is enabled); GIN trigram on `search_text`. Stemmer follows locale
(`finnish` / `swedish` / `english`, else `simple`). Title **A** outranks
description **B**, which outranks `ai_metadata` **C**, which outranks page
text **D**.

`metis_search_reindex_job` is how the cluster agrees there is only one job.
A unique partial index allows at most one row in `RUNNING` or `CANCELLING`.
The row also holds `publication_id`, `bundle_hash`, progress counts,
`last_progress_at` (heartbeat; abandoned reclaim uses this, not start time),
and `restart_after_cancel`.

## Reindex

All entry points go through `MetisLiveIndexTrigger` so the live publication id
is on the job.

- `auto-reindex-on-startup` — boot, only if the index is empty
- `ContentDeployedEvent` — a live publication record was created. Immediate
  publish starts the reindex from this listener.
- `MetisLivePublicationReconciler` — a 60 s poller that is **always**
  ticking (`fixedDelay`, first run 15 s after boot). It is not a timer
  that sleeps until `liveDate`, and a deploy event does not pause it. Each
  tick asks the same question as the event listener: “is the publication
  that is live *now* already indexed?” If the event already started a
  job, the tick sees `isReindexInFlight` and returns. A publication
  scheduled for the future is invisible until `startsAt` is in the past;
  the first tick after that (or a restart that missed the instant)
  starts the job. Immediate publishes do not need this tick.
- `POST …/reindex` — manual start. Query flags below.

`tryStart` inserts `RUNNING`. A second insert hits the unique index (or the
`WHERE NOT EXISTS` filter); duplicate recovery returns no id. The caller gets
HTTP 409 and the current job (`accepted: false`). That is the lock working, not
a cluster failure: one Kubernetes replica wins, the others no-op and stay
healthy. Only one job in the cluster. If the winner dies, no progress for
`abandoned-after-seconds` (3600, from last progress, not start) marks the job
failed so another instance can claim. A clean shutdown marks the local job
`FAILED` and releases the claim.

The two query flags on `POST …/reindex` do different jobs and can be combined.

**`force`** is about **documents**. Default `false` is incremental: a row whose
content hash already matches is skipped (no chat, no embed). `force=true`
rewrites every row even when the hash is unchanged. Use it after a prompt or
embedding-model change that the hash does not fully see, or when you want a
full rebuild. Auto reindex from deploy / the 60 s poller always uses
`force=false`.

**`replace`** is about **the running job**. Default `false` means “start a
job, or 409 if one is already in flight”. `replace=true` asks the current job
to stop (`CANCELLING`), stores `restart_after_cancel` (and the `force` value
as `restart_force`), and when that job actually finishes cancelling, starts a
**new** job with that stored `force`. The first POST returns 202 for the
cancel; the replacement is scheduled after the old work drains. At most
`indexing.concurrency` documents are in flight; the rest stop at the next
`isRunning` check.

`POST …/reindex/cancel` is stop without a follow-up job (`restart_after_cancel`
stays false). No progress for `abandoned-after-seconds` (3600, from last
progress) marks the job failed so another instance can claim.

So: incremental while idle → `POST …/reindex`. Full rebuild while idle →
`?force=true`. Steal a running job and start incremental → `?replace=true`.
Steal it and full-rebuild → `?force=true&replace=true`.

Portal search is ready only after `COMPLETED`. Empty table, failure, or cancel
leave keyword search up. `deleteStaleRows` runs only on full success, so a
partial failure does not wipe the previous index. Treat `COMPLETED` as done
only when `processedCount + skippedCount == totalCount`. An incomplete run
used to be marked completed when queued documents timed out together; that
path now fails the job and keeps the previous index.


## Ranking

`HybridSearchService` builds candidate lists of `max(limit * 3, 30)`, then
returns `limit` (8). Three lists are involved:

1. **Primary FTS** — `websearch_to_tsquery` on `search_vector` (title-weighted).
   This answers “did the citizen type words that are actually in the document?”
2. **Fusion FTS** — usually the same list. If primary has fewer than
   `min-results-before-fallback` hits (default 1, so: if it is empty), a
   second keyword query runs: stemmed OR plus trigram `word_similarity`.
   That is for typos and compound Finnish. It is a sequential scan
   (`unaccent()` + `word_similarity >=` cannot use the GIN index). Threshold
   0.2.
3. **Vector** — embed the query, cosine k-NN. Drop the whole list if the best
   score is below `min-vector-score`, then drop rows below
   `score-drop-off-ratio` × best (0.30 and 0.90 for `bge-m3`, 0.60 and 0.95 for
   `gemini-embedding-2`). Drop-off is not
   applied to a keyword-only list, so a form-name hit stays.

The two (or three) ranked lists are not mixed by raw score. Cosine 0.81 and
`ts_rank` 0.4 are not comparable. Reciprocal rank fusion (RRF) scores a
document from **where it sat in each list**: `weight / (k + rank)` with
`k = 60` (Cormack’s default; a large `k` flattens the gap between rank 1 and
rank 2). Vector weight 0.8, FTS 0.2. A document in both lists gets both
contributions. Primary FTS hits the vector list missed are still inserted so
a strong title match is not lost.

With `k = 60`, vector rank 1 is `0.8/61 ≈ 0.0131` and FTS rank 1 of the same
row is `0.2/61 ≈ 0.0033` (fused ≈ 0.0164). Vector rank 2 of another row is
`0.8/62 ≈ 0.0129`. The first one wins because it was in both lists, not
because its cosine was higher.

Trigram-only ids are not unioned into a vector list that already survived
the no-keyword floor. If vectors decided this is a semantic query, fuzzy keyword
hits do not get tacked on.

Two empty-primary cases (no `websearch_to_tsquery` hit):

1. Embed unavailable (timeout, Ollama busy, `embed-concurrency` reject) →
   keyword ranking, including trigram if primary FTS was empty. `fallback`
   stays false.
2. Embed succeeded, best vector below `min-vector-score-without-keyword` →
   empty results. With `bge-m3` the floor is **0.45** and gibberish (`qwerty`)
   sits in 0.30–0.45 cosine; with `gemini-embedding-2` it is **0.63** and noise
   sits in 0.47–0.62. Random services are worse than nothing. This does not
   fall through to trigram.

Do not lower the floor to recover a weak natural-language query whose metadata
lacks the user’s words. Bump the prompt version and reindex so the helpers
cover that phrasing.

Query embedding is a separate bottleneck from the search thread pool.
`embedQuery` takes a permit from `query.embed-concurrency` (default = query
concurrency, 4). Last 1 000 finished query vectors are cached in-process
(Caffeine, exact string, no TTL) — repeating “library card” does not call
Ollama again on that JVM. Identical in-flight strings join the same future.
Production overlapping *unique* searches all call Ollama; `bge-m3` still
serializes on one Ollama process, extra HTTP just queues there. On GPU that
usually finishes inside the ~3 s embed timeout (search budget minus two
seconds). Local YAML sets the cap to 1 so CPU Ollama does not pile unique
queries until they all miss. Overflow of a *different* uncached string is
FTS-only, not `fallback: true`. Index-time `embed()` does not use this
semaphore.

Search executor: 4 threads, queue 50, `AbortPolicy` → `fallback: true`, 5 s
timeout. Indexing pool: default 1.

## Worker API

| Method | Path | Role |
| --- | --- | --- |
| GET | `/worker/rest/api/metis/status` | `ROLE_Authorized` |
| GET | `/worker/rest/api/metis/search/status` | `ROLE_Authorized` |
| POST | `/worker/rest/api/metis/search/reindex` | `ROLE_ASSET_ADMIN` |
| POST | `/worker/rest/api/metis/search/reindex?replace=true` | `ROLE_ASSET_ADMIN` |
| POST | `/worker/rest/api/metis/search/reindex/cancel` | `ROLE_ASSET_ADMIN` |

No `/metis/**` catch-all; a new capability is closed until it has a role
(`PropertyAuthorizationTest`). 409 means another job is in flight. Poll status
until it is no longer `RUNNING` or `CANCELLING`.

The worker sidebar item and status/reindex page are shown when `metis` is in
`eveli.tenant-features` (or on the user profile). That flag is UI only; it
does not start the platform. Without `eveli.metis.enabled` the page still
opens and says Metis is off. `GET /metis/status` is the one-line
summary per capability. Detail lives on the capability’s own status endpoint.

Useful classes if you are reading the code: `SiteContentReader`,
`SiteSearchDocumentBuilder`, `IndexingService`, `HybridSearchService`,
`EmbeddingService`, `MetisLiveIndexTrigger`, `GamutSiteSearchController`,
`useBackendSearch`, `useSemanticResults`.

## Tests

Library unit tests live in `metis-client`. Host wiring, the portal controller,
and PostgreSQL integration live in `eveli-client`. There are no frontend unit
tests for `useBackendSearch` / `useSemanticResults`. Ranking quality against
a live municipal index is the local eval in [Measured results](#measured-results),
not CI.

`StubEmbeddingModel` is a test double, not a test.

### `metis-client`

**`EmbeddingServiceTest`** — query-embed semaphore and cache.

- `twoDifferentQueriesEmbedWhenPermitsAreTwo` — two unique queries both call the model when concurrency is 2
- `aSecondDifferentQueryFailsFastWhenPermitsAreOne` — a second unique query is rejected when the cap is 1
- `theSameQueryInFlightSharesOneEmbed` — identical in-flight strings join one future
- `aCachedQueryDoesNotEmbedAgain` — a finished query vector is served from Caffeine

**`MetisClientImplTest`** — platform capability status for search.

- `searchDisabledWhenTheSearchClientIsMissing` — no search client → `DISABLED`
- `searchIsReadyWhenTheIndexHasDocuments` — documents present → `READY`
- `searchIsNotReadyUntilAReindexHasCompleted` — index exists but job not `COMPLETED` → `NOT_READY`
- `searchIsNotReadyWhenTheIndexIsEmpty` — empty index → `NOT_READY`
- `searchReportsErrorWhenTheIndexCannotBeRead` — status read failure → `ERROR`

**`SiteSearchDocumentBuilderTest`** — what becomes a document.

- `oneDocumentPerWorkflowLinkWithTopicContent` — one row per workflow × locale, topic text folded in
- `topicsBehindAuthenticationAreIndexed` — `topic.auth` workflows are still documents
- `textOfTopicsSharedByManyWorkflowsIsLeftOut` — listing pages above the generic-topic threshold drop body/headings
- `pageTextIsTruncatedAndContentHashTracksTheContent` — page-char cap and hash follows the truncated text
- `contentHashChangesWhenTheOwningTopicOrTheModelChanges` — owning topic or embedding model id invalidates the hash
- `llmContextStaysWithinItsCharacterBudget` — metadata prompt input is truncated

**`SiteContentReaderTest`** — limaone read at index time.

- `aMissingBundleFailsInsteadOfReportingAnEmptySite` — no bundle → `ContentUnavailableException`
- `aMissingArticleProgramFailsInsteadOfReportingAnEmptySite` — no article program → same
- `noConfiguredLocaleResolvingToASiteFails` — none of the locales resolve → same
- `aResolvedSiteWithoutWorkflowsReadsAsZeroDocuments` — empty site is a successful zero, not a failure
- `readsTheCachelessBundleSoThatADeploymentIsNotIndexedFromTheOldSite` — `getCachelessBundle().withCacheless()` is the read used

**`MetadataGeneratorTest`** — chat helpers for `ai_metadata`.

- `chatFailureFallsBackToTheWorkflowDescription` — LLM error → description-only metadata, document still indexes
- `aBlankLlmDescriptionKeepsTheWorkflowDescription` — empty model output does not wipe the workflow description

**`HybridSearchServiceTest`** — fusion when one side is missing (mocked FTS / vector).

- `embeddingFailureWithEmptyPrimaryReturnsTrigramHits` — embed fail + empty primary → keyword/trigram ranking, not empty
- `weakVectorWithEmptyPrimaryReturnsNothing` — embed ok, best vector below 0.45, no FTS → empty
- `embeddingFailureWithPrimaryHitsReturnsKeywordRanking` — embed fail + title hits → FTS-only ranking, `fallback` stays false

### `eveli-client`

**`MetisSearchIntegrationTest`** — PostgreSQL + real ranking/index jobs (Testcontainers).

- `indexesTheSiteAndFindsServicesByKeywordAndByMeaning` — end-to-end index then hybrid hit by title and by paraphrase
- `hybridFusesBothListsAndKeepsEveryCandidate` — a document in FTS and vector keeps both contributions
- `hybridStillReturnsKeywordHitsWhenQueryEmbeddingFails` — embed throws → FTS ranking
- `hybridStillReturnsKeywordHitsWhenQueryEmbeddingTimesOut` — embed timeout → FTS ranking
- `weakMatchesAreDroppedRatherThanPaddingTheResults` — poor cosine neighbours are not returned as filler
- `resultsAreCappedAtTheConfiguredMaximum` — `limit` / `max-results` clamp
- `unchangedDocumentsAreSkippedAndRemovedOnesAreDeleted` — incremental skip by hash; stale rows deleted on success
- `aFailedContentReadLeavesTheIndexAlone` — `ContentUnavailableException` does not wipe rows
- `aSiteWithoutWorkflowsClearsTheIndex` — successful empty site deletes previous documents
- `onlyOneReindexJobRunsAtATime` — second `tryStart` is rejected
- `aPublicationIdIsRecordedAndCountsAsIndexedUntilTheJobFails` — `isPublicationIndexed` follows job outcome
- `aLivePublicationStartsAReindexJob` — deploy path stamps the live publication id
- `aStaleBundleHashDoesNotCountAsThePublicationBeingIndexed` — completed job for an old hash is not “this publication”
- `callerSuppliedLimitAndQueryLengthAreClamped` — oversize `limit` and `q` are truncated
- `aJobIsOnlyReclaimedOnceItStopsReportingProgress` — abandoned reclaim uses `last_progress_at`, not start time
- `shutdownReleasesTheClaimOfAJobThisInstanceStarted` — JVM stop fails the running job so another instance can claim
- `searchIsRejectedForLocalesThatAreNotIndexed` — query locale with no rows does not leak other locales
- `queuedDocumentsDoNotExpireBeforeTheyStart` — document timeout is from start, not from queue
- `aPartialReindexDoesNotDeleteThePreviousIndex` — failed/incomplete job skips `deleteStaleRows`
- `aCancellingJobStillHoldsTheSingleFlightGuard` — `CANCELLING` still blocks a second start
- `portalSearchFallsBackWhenNoReindexHasRun` — empty job table → portal `fallback: true`
- `portalSearchFallsBackUntilAReindexHasCompleted` — `RUNNING` is not ready for the portal
- `portalSearchFallsBackAfterAFailedReindexUntilTheNextCompletedJob` — `FAILED` stays on keyword until a later `COMPLETED`
- `cancellingAJobReleasesTheClaimAndLeavesTheIndex` — `POST cancel` → `CANCELLED`, rows remain
- `replaceCancelsTheRunningJobAndStartsAnother` — `replace=true` then a new `RUNNING` job
- `primaryFtsHitsJoinHybridEvenWhenVectorAlreadyHasNeighbours` — title match is inserted even if k-NN missed it
- `weakVectorMatchesAreDroppedWhenKeywordSearchIsEmpty` — empty primary + below 0.45 → no results
- `aMisspelledKeywordFallsThroughToTrigramSearch` — empty primary FTS uses trigram fallback

**`GamutSiteSearchControllerTest`** — portal `GET /portal/site/search` (mocked client).

- `overTheRateLimitFallsBackWithoutCallingSearch` — 11th request / 10 s from the same `remoteAddr` is `fallback: true`
- `aSearchTimeoutFallsBackToKeywordSearch` — Uni timeout → `fallback: true`
- `aSearchErrorFallsBackToKeywordSearch` — unexpected failure → `fallback: true`
- `anIndexThatIsNotReadyFallsBackWithoutCallingSearch` — `isIndexReadyForPortal` false skips ranking
- `aFailedIndexStatusCheckFallsBackWithoutCallingSearch` — status throw is treated as not ready

**`MetisLiveIndexTriggerTest`** — whether a live publication starts a job.

- `startsAJobWhenTheLivePublicationIsNotYetIndexed`
- `skipsWhenAReindexIsAlreadyInFlight`
- `skipsWhenTheLivePublicationIsAlreadyIndexed`
- `skipsAPublicationThatIsNotLiveYet`
- `skipsWhenReindexOnDeploymentIsOff`
- `aManualStartStampsTheLivePublicationId` — `POST reindex` still records the live id

**`MetisReindexListenerTest`** — Spring entry points.

- `aContentDeployedEventStartsAReindexWhenEnabled`
- `aContentDeployedEventIsIgnoredWhenReindexOnDeploymentIsOff`
- `startupReindexesAnEmptyIndex`
- `startupSkipsWhenTheIndexAlreadyHasDocuments`

**`MetisLivePublicationReconcilerTest`** — 60 s poller.

- `checkLivePublicationDelegatesWhenReindexOnDeploymentIsOn` — tick calls `startIfLivePublicationChanged`
- `checkLivePublicationIsANoOpWhenReindexOnDeploymentIsOff`

**`AssetsPublicationControllerEventTest`** — who publishes `ContentDeployedEvent`.

- `anImmediatePublicationPublishesContentDeployedEvent`
- `aScheduledPublicationDoesNotPublishUntilItIsLive`
- `aReadOnlyControllerDoesNotPublish`

**`LivePublicationsTest`** — which deployment is live *now*.

- `aNullOrPastLiveDateIsAlreadyLive`
- `aFutureLiveDateIsNotLive`
- `aFuturePublicationIsIgnoredUntilItsLiveDate`
- `aLaterStartsAtOvertakesThePreviousLivePublication`
- `theLaterCreatedAtWinsWhenStartsAtIsEqual`
- `anEmptyListHasNoLivePublication`

**`PropertyAuthorizationTest`** (Metis cases only) — worker URL roles.

- `testMetisPlatformStatusIsReadableByAnyWorker` — `GET /metis/status` → `ROLE_Authorized`
- `testAnUnknownMetisCapabilityIsNotReadable` — no catch-all under `/metis/**`
- `testMetisSearchStatusIsReadableByAnyWorker` — `GET …/search/status` → `ROLE_Authorized`
- `testMetisSearchReindexNeedsTheAssetAdminRole` — `POST …/reindex` → `ROLE_ASSET_ADMIN`
- `testMetisSearchReindexCancelNeedsTheAssetAdminRole` — `POST …/reindex/cancel` → `ROLE_ASSET_ADMIN`

## Measured results

Live municipal index, a few hundred documents, 11 Sep 2026, against
`GET /portal/site/search`. Not CI. Query sets are local, not in the repo.

Finnish set of 20 citizen-worded queries: 20/20 first (and top 3 / top 5),
mean rank 1.00, about 2 results per query (1–8), 0 fallbacks. Cold p50 ~200 ms
from an uncached pass; warm ~30 ms. The 20/20 run itself was cache-warm
(~38 ms) — that is not cold.

An older Finnish 20 had two misses: one paraphrase with no keyword overlap
scored ~0.39 and came back empty (below 0.45); another ranked the intended
service 5th behind a related one. Hybrid 18/20 first on that set (19/20
without the 0.45 floor). Keyword on the same 20 was 9/20.

English and Swedish sets of 7 scored 7/7 first. Two Swedish cases ranked 2nd
(a related service above the intended one).

The numbers above are `bge-m3` with `llama3.2` metadata.

### Gemini on Vertex AI

Local index of 258 documents (120 fi, 115 sv, 23 en), 25 Sep 2026,
`gemini-embedding-2` at 1024 dimensions and `gemini-3.1-flash-lite` metadata,
against the real search services. The query set is the local, gitignored
`eval-queries.json` next to this file, now with 19 noise queries.

With the Gemini thresholds (0.60 / 0.63 / 0.95): all 38 real queries first,
including the two former Finnish misses and the two Swedish soft spots, about
1.8 results per query, and none of the 19 noise queries returned anything.
With the `bge-m3` thresholds all 19 noise queries returned results. A full
reindex took 109 s at indexing concurrency 4, against hours on CPU Ollama.

## Limits

Single tenant (`cockpit_id` is not on the documents). Only `en` / `fi` / `sv`
have stemmers. Metadata generation on CPU is tens of seconds to minutes per
document; a few hundred documents against CPU Ollama takes hours. Disabled /
`inHouse` / non-dev `devMode` workflows never reach the site JSON.
Login-required services are indexed; opening the form is still gated.
Gibberish with no keyword hit is rejected below the no-keyword floor (0.45 for
`bge-m3`, 0.63 for `gemini-embedding-2`) — bump the prompt version rather than
lowering the floor. Trigram fallback is a sequential scan and only
runs when primary FTS is empty.
