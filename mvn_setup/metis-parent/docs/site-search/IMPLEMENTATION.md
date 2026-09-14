# Metis semantic site search: how it works

Enablement (flags, Flyway, Ollama, GCloud) is in the
[Metis README](../../README.md#semantic-site-search). Properties are in
[docs/README_CONFIG_PROPERTIES.md](../../../../docs/README_CONFIG_PROPERTIES.md).

Citizens search in Gamut. Staff use Eveli. Authors publish a limaone **bundle**
(pages, topics, workflow links) as a **publication**, which can go live
immediately or at a future `liveDate`. Metis is the Digiexpress AI platform;
semantic site search is the first capability on it.

The portal already filtered the site JSON in the browser: form titles, topic
names, phones, hyperlinks. Semantic site search replaces the **form and topic** lists
when the backend returns a real ranking. Phones and hyperlinks stay on that
client-side filter. The point is queries in everyday language that do not
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

The library has no Spring MVC. Ranking does not live in eveli-client. The
portal calls `GET /portal/site/search`, not a Metis-branded path, so something
else could sit behind it later.

## Search path

The box is `GPopoverSearch`. After 350 ms idle, `useBackendSearch` calls
`GET /portal/site/search?q=…&locale=fi` and waits up to 8 s (the server times
out at 5 s, so a late success is still used). The controller is public because
`/portal/site/**` already is. Worker APIs under `/worker/rest/api/metis/search`
need a JWT.

If the latest reindex job is not `COMPLETED` (including never having run), the
response is `{ fallback: true, results: [] }` and the portal stays on keyword
search. Otherwise hybrid search runs: FTS, embed the query, k-NN, RRF. Hits
carry `workflowId` and `topicId`; the portal maps those ids onto the site JSON
it already has. Display names are not a join key.

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

`score` is RRF, not cosine. `vectorScore` / `ftsScore` are the raw list
contributions when that side had the hit.

Fallback to keyword (`fallback: true`, or 404 → `unavailable` if the
controller is not even there):

- no completed job (`NONE`, empty table, `RUNNING`, `CANCELLING`, `FAILED`,
  `CANCELLED`)
- per-IP rate limit (10 / 10 s, `remoteAddr`, not `X-Forwarded-For`)
- search queue full (`concurrency` 4, `queue-capacity` 50, `AbortPolicy`)
- 5 s timeout or unexpected error
- every hit fails to map onto the live site

A slow or rejected query embed is **not** this path. Hybrid continues as
keyword ranking and `fallback` stays false, so the citizen still gets server
FTS.

`FAILED` / `CANCELLED` remain on `GET /worker/rest/api/metis/search/status` so
someone can re-run reindex. The portal does not wait on them.

The rate limiter is in-memory per JVM. Behind a proxy that overwrites
`remoteAddr`, everyone shares one bucket. Trusting `X-Forwarded-For` on a
public endpoint would be worse.

### Frontend

`useBackendSearch` fetches. `useSemanticResults` maps. While the request is
`pending`, form and topic lists render empty (`aria-busy`) so keyword results
do not flash then get replaced. An empty array after a real answer is
no-results, not a fallback.

- `unavailable` (404/401/403): keyword for the rest of the session
- `fallback: true`, non-OK, or network error: keyword for this query
- some ids missing: keep the mapped ones, log the rest
- every id missing: keyword (index and live site disagree)
- empty array: no-results

The same workflow can sit on more than one topic. The lookup prefers a
`searchOnly` topic when one exists, which is the synthetic search topic the
portal already uses for “open form”.

## What gets indexed

One row per workflow link per locale, unique on `(workflow_id, locale)`. A
result is a form, not a topic and not an article. Topics only contribute text
(and a `topic_id` so the portal can highlight the page). Phones, hyperlinks,
and internal pages are not documents. A workflow with no owning topic is
skipped.

Limaone workflow links barely have copy (name, form name, flow name), so
`SiteSearchDocumentBuilder` pulls from every topic that links to that
workflow:

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

`SiteContentReader` is the only limaone call at index time.

`getBundle()` is debounced (~30 s). `getCachelessBundle()` is a shared ~5 s
view. The reader calls `getCachelessBundle().withCacheless()` so this read gets
a **new** empty debounce instead of the long-lived Runtime’s stale HEAD.

It runs the article program per configured locale (`en, fi, sv` by default). A
missing locale is logged and skipped. If none resolve, the job fails with
`ContentUnavailableException` and does not touch rows.

The limaone world hash of that view is stored as `bundle_hash`. A completed
job only counts as “this publication is indexed” while that hash still matches
cacheless HEAD, so a read that still saw the previous bundle cannot skip the
new one forever.

There has to be a live publication (`startsAt` null or not in the future).
Among live ones, latest `startsAt` wins, then `createdAt`. Authoring import
does not fire a deploy event. A future `liveDate` is indexed when it becomes
live, not when the record is created. Immediate publishes start a job on the
event.

## Metadata, embeddings, hash

For each document, `llama3.2` (locally) is asked for a short description,
synonyms, and related phrases in that locale. Prompt is in `MetadataGenerator`.
The result is `ai_metadata`: concatenated onto the embedding input, and FTS
band C. The LLM only sees title, description, and topic names (truncated);
page body is already in `searchText`. Two failed chat attempts fall back to
description-only metadata. A missing description does not fail the document.

Bump `indexing.metadata-prompt-version` when the prompt changes. It is part of
the content hash, so a normal reindex rewrites every row.

Embeddings are `bge-m3`, cosine, column `VECTOR(1024)`.
`indexing.embedding-dimension` only **checks** that width. A mismatch fails
the job. Changing width is a new migration.

Index-time `embed()` is not gated by the query-embed semaphore; indexing
concurrency (default 1) already limits how many documents run. The 600 s
document timeout starts when that document starts, not when it was queued.

Content hash is SHA-256 of locale, owning topic id, title, search text,
embedding model id, and the prompt version. Unchanged hashes are skipped.
Switching the embedding model or bumping the prompt version invalidates them.
`force=true` ignores hashes. `ai_metadata` is not in the hash — the hash is
computed before the chat call, and the prompt version stands in for “helpers
might have changed”.

## Store

Flyway location `classpath:db/metis/search`, appended only while search is
enabled. Semantic site search owns **V4_x** (`V4_1__metis_search.sql`); later
capabilities take V5_x+. A database that never enables search never needs
pgvector. Extensions: `vector`, `pg_trgm`, `unaccent` (app role
`CREATE EXTENSION`, or a DBA pre-creates them — typical on Cloud SQL).

`metis_search_index`: unique `(workflow_id, locale)`; GIN on `search_vector`;
HNSW cosine on `embedding`; GIN trigram on `search_text`. Stemmer follows
locale (`finnish` / `swedish` / `english`, else `simple`). Title **A**
outranks description **B**, which outranks `ai_metadata` **C**, which outranks
page text **D**.

`metis_search_reindex_job` is the single-flight claim (unique partial index on
`RUNNING` | `CANCELLING`), plus `publication_id`, `bundle_hash`, progress,
`last_progress_at` (heartbeat; abandoned reclaim uses this, not start time),
and `restart_after_cancel`.

## Reindex

All entry points go through `MetisLiveIndexTrigger` so the live publication id
is on the job.

- `auto-reindex-on-startup` — boot, only if the index is empty
- `ContentDeployedEvent` — a live publication record was created
- `MetisLivePublicationReconciler` — every 60 s, in-process: “is the
  publication that is live *now* already indexed?” It does not sleep until
  `liveDate`; a scheduled publication is picked up on the first tick after it
  becomes live (and after a restart that missed the instant)
- `POST …/reindex` — manual; `force=true` rewrites hashes; `replace=true`
  cancels the current job then starts another

`tryStart` inserts `RUNNING`. A second insert hits the unique index; the
caller gets 409 and the current job (`accepted: false`). Only one job in the
cluster.

Portal search is ready only after `COMPLETED`. Empty table, failure, or cancel
leave keyword search up. `deleteStaleRows` runs only on full success, so a
partial failure does not wipe the previous index. Treat `COMPLETED` as done
only when `processedCount + skippedCount == totalCount`. An incomplete run
used to be marked completed when queued documents timed out together; that
path now fails the job and keeps the previous index.

Cancel: at most `indexing.concurrency` documents are in flight; the rest stop
at the next `isRunning` check. `replace=true` sets `restart_after_cancel`.
`POST …/reindex/cancel` stops without starting another. No progress for
`abandoned-after-seconds` (3600, from last progress) marks the job failed so
another instance can claim.

On shutdown, `MetisSearchClientImpl.shutdown()` fails the running job so the
unique index is released. The reindex executor is one non-daemon thread (a
daemon would die on JVM exit and leave `RUNNING`). It is a plain
`ExecutorService` so it does not steal Spring `@Async`. Blocking Thena SQL
runs on the Vert.x worker pool, never on the event loop and never on that
single reindex thread — awaiting SQL on the loop deadlocks.

## Ranking

`HybridSearchService` builds candidate lists of `max(limit * 3, 30)`, then
returns `limit`.

Primary FTS is `websearch_to_tsquery` on `search_vector` (title-weighted):
did the citizen type words that are actually in the document?

If that list already has `min-results-before-fallback` hits (default 1), it is
also the fusion list, so weaker keyword hits can still break ties. If it is
empty, a stemmed-OR plus trigram `word_similarity` fallback runs. That is a
sequential scan (`unaccent()` + `word_similarity >=` cannot use the GIN
index). Threshold 0.2. It is for typos and compound Finnish, not a second
semantic path.

Vector: embed the query, cosine k-NN, drop the whole list if the best score is
below 0.30, then drop rows below 0.90 × best. Drop-off is not applied to a
keyword-only list, so a form-name hit stays.

RRF, not raw score mixing — cosine 0.81 and `ts_rank` 0.4 are not comparable.
Each hit gets `weight / (k + rank)` with `k = 60`. Vector weight 0.8, FTS
0.2. A document in both lists gets both. Primary FTS hits the vector list
missed are still inserted so a strong title match is not lost.

With `k = 60`, vector rank 1 is `0.8/61 ≈ 0.0131` and FTS rank 1 of the same
row is `0.2/61 ≈ 0.0033` (fused ≈ 0.0164). Vector rank 2 of another row is
`0.8/62 ≈ 0.0129`. The first one wins because it was in both lists, not
because its cosine was higher.

Trigram-only ids are not unioned into a vector list that already survived
0.45. If vectors decided this is a semantic query, fuzzy keyword hits do not
get tacked on.

Two empty-primary cases:

1. Embed unavailable (timeout, Ollama busy, `embed-concurrency` reject) →
   keyword ranking, including trigram if primary FTS was empty. `fallback`
   stays false.
2. Embed succeeded, best vector below **0.45** → empty. Gibberish (`qwerty`)
   sits in 0.30–0.45. Random services are worse than nothing. This does not
   fall through to trigram.

Do not lower 0.45 to recover a weak NL query whose metadata lacks the user’s
words. Bump the prompt version and reindex.

`embedQuery` takes a permit from `query.embed-concurrency` (default = query
concurrency, 4). Last 1 000 finished query vectors are cached in-process
(Caffeine, exact string, no TTL). Identical in-flight strings join the same
future. Production overlapping unique searches all call Ollama; `bge-m3` still
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

The worker page is chrome (`metis` in `eveli.tenant-features` or the user
profile). It does not start the platform; without `eveli.metis.enabled` the
page still opens and says Metis is off. `GET /metis/status` is the one-line
summary per capability. Detail lives on the capability’s own status endpoint.

Useful classes if you are reading the code: `SiteContentReader`,
`SiteSearchDocumentBuilder`, `IndexingService`, `HybridSearchService`,
`EmbeddingService`, `MetisLiveIndexTrigger`, `GamutSiteSearchController`,
`useBackendSearch`, `useSemanticResults`.

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

## Limits

Single tenant (`cockpit_id` is not on the documents). Only `en` / `fi` / `sv`
have stemmers. Metadata generation on CPU is tens of seconds to minutes per
document; a few hundred documents against CPU Ollama takes hours. Disabled /
`inHouse` / non-dev `devMode` workflows never reach the site JSON.
Login-required services are indexed; opening the form is still gated.
Gibberish with no keyword hit is rejected below 0.45 — bump the prompt version
rather than lowering the floor. Trigram fallback is a sequential scan and only
runs when primary FTS is empty.
