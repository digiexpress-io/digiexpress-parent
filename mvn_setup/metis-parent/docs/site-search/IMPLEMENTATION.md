# Metis site search: how it works

This document explains the site search capability from first principles: what a
citizen types in the portal search box, how that becomes a ranked list of
services, and how those services got into the index.

It is written for someone who has not seen this code. Ranking, the document
model, and the query path are here. How to turn the capability on (flags,
Flyway, Ollama, GCloud, local Docker) is in the
[Metis README, Site search](../../README.md#site-search). Properties are listed
in [docs/README_CONFIG_PROPERTIES.md](../../../../docs/README_CONFIG_PROPERTIES.md).
Review talking points are in [SENIOR_REVIEW.md](SENIOR_REVIEW.md).

---

## Contents

1. [What this is](#what-this-is)
2. [Words used here](#words-used-here)
3. [Where the code lives](#where-the-code-lives)
4. [How a citizen search works](#how-a-citizen-search-works)
5. [Portal contract](#portal-contract)
6. [Frontend mapping](#frontend-mapping)
7. [What is indexed](#what-is-indexed)
8. [How content is read](#how-content-is-read)
9. [Metadata and embeddings](#metadata-and-embeddings)
10. [Store](#store)
11. [Reindex](#reindex)
12. [Query ranking](#query-ranking)
13. [Concurrency and threading](#concurrency-and-threading)
14. [Worker API and UI](#worker-api-and-ui)
15. [Class map](#class-map)
16. [Measured results](#measured-results)
17. [Limits](#limits)

---

## What this is

Digiexpress is a municipal case-management platform. Staff use the **worker**
UI (Eveli). Citizens use the **portal** (Gamut). Content authors publish a
**bundle**: pages, topics, and **workflow links** (the buttons that open a
form). A **publication** is a named, versioned snapshot of that bundle. It can
go live immediately or at a future `liveDate`.

**Metis** is the AI platform for Digiexpress. Site search is the first
**capability** on that platform. It indexes each workflow link × locale from
the **live published** limaone bundle into PostgreSQL, and answers
`GET /portal/site/search` with a hybrid of full-text search and vector search.

The portal already had client-side keyword search over the site JSON the
browser loaded: form titles, topic names, phone numbers, hyperlinks. Site
search **replaces** the form and topic lists when the backend answers a real
result list. Phones and hyperlinks stay client-side.

Typical need: the citizen describes a situation in everyday language that does
not appear in the form title. Keyword search cannot do that. Semantic search
can, if the indexed metadata actually contains that language.

Two stores sit on every indexed document:

- **Full-text (FTS):** PostgreSQL `tsvector`. Keyword matching with language
  stemmers (`finnish`, `english`, `swedish`).
- **Vector:** pgvector. The embedding model (`bge-m3`, 1024 numbers) places
  similar *meanings* near each other, even when the words differ.

At query time those two ranked lists are fused with **reciprocal rank fusion
(RRF)**. If anything on the backend path is not ready, the portal does not
show an error: it keeps the keyword search it already had.

---

## Words used here

| Word | Meaning |
| --- | --- |
| **eveli-client** | Spring host: REST, Flyway, permissions, auto-config. Apps `@Import` it. |
| **metis-client** | Library: search, index, SQL, ranking. No Spring MVC. |
| **limaone** | Content compiler. The article program is the site JSON the portal already ships. |
| **Thena** | SQL toolkit. Annotations generate typed queries against a Vert.x `Pool`. |
| **Flyway** | Schema migrations. One history table for the whole app. |
| **Gamut** | Citizen portal. |
| **Publication / deployment** | A published content snapshot. “Live” means its start time is now or in the past. |
| **Workflow link** | A service / form button on a topic. This is what search returns. |
| **Topic** | A page that groups workflow links. Topics are not searchable on their own; their text describes the workflows they contain. |
| **Reindex** | Rebuild or incrementally update `metis_search_index` from the live bundle. |
| **`fallback: true`** | Backend tells the portal “use client-side keyword search.” Not an HTTP error. |
| **Primary FTS** | `websearch_to_tsquery` against `search_vector`. Title-weighted keyword hits. |
| **RRF** | Reciprocal rank fusion: `weight / (k + rank)`, not raw score mixing. |

---

## Where the code lives

| Layer | Package / module | Owns |
| --- | --- | --- |
| Library | `io.resys.metis.search` in `metis-client` | Documents, index, query, reindex job |
| Shared AI | `io.resys.metis.spi.ai` | `EmbeddingService`, `StructuredChatService` (capability-agnostic) |
| Host | `eveli-client` | Spring beans, REST, Flyway, permissions, live-publication trigger |
| Portal | `gamut-api` (`useBackendSearch`, `backend-results`) | Fetch, fallback, mapping onto the live site |
| Worker UI | `eveli-primitives/eveli-metis` | Status, progress, reindex buttons |

The library does not know about Spring MVC. The host does not implement ranking.
The portal asks for **site search**, not for Metis: `GET /portal/site/search` is
deliberately unbranded so a later implementation could sit behind the same
path.

---

## How a citizen search works

```
Citizen types in GPopoverSearch
        │
        ▼
useBackendSearch  (debounce 350 ms, abort after 8 s)
        │  GET /portal/site/search?q=…&locale=fi
        ▼
GamutSiteSearchController          public, under /portal/site/**
        │  rate limit, index-ready check
        ▼
MetisSearchClientImpl.query()      SearchMode.HYBRID
        │  search executor, 5 s timeout
        ▼
HybridSearchService
   ├── FtsSearchService            PostgreSQL tsvector
   └── VectorSearchService         embed query → pgvector k-NN
        │
        ▼
JSON { query, locale, fallback, results[] }
        │
        ▼
useSemanticResults maps workflowId / topicId onto the live site
```

1. The citizen types in the portal search box (`GPopoverSearch`).
2. After 350 ms of no typing, `useBackendSearch` calls
   `GET /portal/site/search?q=…&locale=fi`. The browser waits up to 8 seconds
   (longer than the 5 second server timeout) so a late success is kept.
3. `GamutSiteSearchController` is public. `SpringSecurityPolicy` already
   allows anyone on `/portal/site/**`. Worker APIs under
   `/worker/rest/api/metis/search` require a JWT.
4. If the latest reindex job is not `COMPLETED` (including “no job has ever
   run”), the API returns `{ fallback: true, results: [] }` and the portal
   keeps keyword search.
5. Otherwise Metis runs hybrid search: keyword FTS, then embed the query, then
   nearest-neighbour in pgvector, then RRF.
6. Hits are `{ workflowId, topicId, title, score, … }`. The portal maps those
   **ids** onto the site JSON it already has. Display names are never used as
   a join key.
7. Forms and topics in the result list are replaced by that ranking. Phone
   numbers and hyperlinks stay client-side on purpose.

A **reindex** is a background job: read the live limaone bundle, skip unchanged
documents by content hash, ask the chat model for citizen phrasing, embed,
upsert, delete rows that no longer exist. Only one job may run in the whole
cluster (database unique index). The portal stays on keyword search until that
job reaches `COMPLETED`.

---

## Portal contract

`GET /portal/site/search?q=&locale=&limit=`

| Field | Rule |
| --- | --- |
| `q` | Required. Truncated at `query.max-query-chars` (400). |
| `locale` | Required. Comparison is case-insensitive. Only rows for that locale are searched. |
| `limit` | Optional. Capped at `query.max-results` (8). Default is `query.default-limit` (8). Keep those two equal or callers silently get fewer rows than they asked for. |

Successful body:

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

`score` is the RRF fusion score, not cosine similarity. `vectorScore` and
`ftsScore` are the raw contributions when that list had the hit.

### When the portal does not use the backend list

The controller or the client answers in a way that means “use keyword search”:

| Situation | What the portal sees |
| --- | --- |
| Capability off (no controller bean) | HTTP 404 → `unavailable` |
| Index never completed (`NONE`, empty job table, `RUNNING`, `CANCELLING`, `FAILED`, `CANCELLED`) | `{ fallback: true }` |
| Per-IP rate limit (default 10 / 10 s, keyed by `remoteAddr`, not `X-Forwarded-For`) | `{ fallback: true }` |
| Search queue full (`query.concurrency` 4, `queue-capacity` 50, `AbortPolicy`) | `{ fallback: true }` |
| Server timeout (5 s) or unexpected error | `{ fallback: true }` |
| Every hit fails to map onto the live site | `useSemanticResults` returns `undefined` → keyword |

A slow or busy query embed does **not** take that path. Hybrid continues as
keyword ranking (`fallback: false`) so the citizen still gets server FTS.

`FAILED` and `CANCELLED` stay visible on
`GET /worker/rest/api/metis/search/status` so an operator can re-run reindex.
The portal does not wait on a failed job: it stays on keyword search until the
next `COMPLETED`.

The rate limiter is in-memory per JVM, keyed by `request.getRemoteAddr()`. It
does not read `X-Forwarded-For`. Behind a reverse proxy that replaces
`remoteAddr` with the proxy, every citizen shares one bucket. That is
intentional: a shared fallback is better than trusting a client-supplied
header on a public endpoint.

---

## Frontend mapping

`useBackendSearch` owns the fetch. `useSemanticResults` owns the mapping.

While the request is in flight (`pending`), form and topic lists render empty
(`aria-busy`) so keyword results do not flash then get replaced. A genuine
empty list (index ready, query answered, nothing matched) is “no results”, not
a fallback.

| Backend state | Portal lists |
| --- | --- |
| `pending` | Empty (`aria-busy`) |
| `unavailable` (404/401/403) | Keyword search for the rest of the session |
| `{ fallback: true }` or non-OK / network error | Keyword search for this query |
| Results, all ids map | Semantic forms and topics, in backend order |
| Results, some ids missing from the live site | Mapped hits kept; missing ones logged |
| Results, **every** id missing | Keyword search (site and index disagree) |
| Results, empty array | No-results |

Mapping is by `workflowId` / `topicId`, never by display name. The same
workflow can appear on more than one topic; the lookup prefers a
`searchOnly` topic as the owner when one exists, so the “open form” button
lands on the synthetic search topic the portal already uses for that.

Phones and hyperlinks are **never** taken from the backend. They stay on the
client-side keyword filter of the site JSON.

---

## What is indexed

One row in `metis_search_index` per **workflow link per locale**. Unique key:
`(workflow_id, locale)`. Locales never mix: a Finnish query only sees Finnish
rows.

A result is a workflow link, not an article and not a topic. Topics exist in
the index only as text that describes the workflows they contain, plus a
`topic_id` so the portal can highlight the owning page.

Non-workflow links (phones, hyperlinks, internal pages) are not documents.

### Where the text comes from

A workflow link in limaone carries little copy: name, form name, flow name.
`SiteSearchDocumentBuilder` therefore pulls text from every topic that links
to that workflow:

| Source | Used as |
| --- | --- |
| Workflow name | Document `title`, FTS band **A** |
| Form name + flow name | Document `description`, FTS band **B** |
| Owning topic names | First name is `category`; all names go into supplemental and LLM context |
| Headings on owning topics | Supplemental, FTS band **D** |
| Page markdown, stripped, truncated to `indexing.max-page-chars` (800) | Supplemental, FTS band **D** |
| Sibling link titles on those topics | Supplemental, FTS band **D** |
| Chat-model helpers | `ai_metadata`, FTS band **C**, also concatenated onto the embedding input |

Topics that link to **more than** `indexing.generic-topic-threshold` workflows
(default 4, so five or more) are treated as generic listing pages. Their body,
headings and sibling titles are dropped so an “all services” page does not
pollute every document. The topic title is still the category and is still
handed to the metadata model.

A workflow with no owning topic is skipped. A topic with no workflow links
produces no document of its own.

### Visibility: what limaone already hid

The indexer does not apply its own draft / disabled / auth filters. It indexes
whatever the **article program** emits for an anonymous participant at
`OffsetDateTime.now()`.

The dummy participant is `metis-indexer`, `anon: true`. That is the same
visibility a logged-out citizen has.

limaone, when compiling the site, already drops:

- `disabled` workflows
- `inHouse` workflows
- `devMode` workflows, unless the compiler is in dev

Those never appear in `LocalizedSite`, so they are not indexed. Metis does not
re-check those flags.

**Login-required services are indexed.** The article program does not hide
`topic.auth` from the anonymous site JSON. The portal still lists them; the UI
hides “open form” via `isFormLinkEnabled`. Search can find them; opening the
form is still gated. Hiding them from search would mean hiding them from the
site tree as well, or the two surfaces would disagree.

---

## How content is read

`SiteContentReader` is the only place that talks to limaone at index time.

Indexing must see a publication that just went live. limaone `getBundle()` is
debounced (~30 s). `getCachelessBundle()` is a shared ~5 s view. The reader
calls `getCachelessBundle().withCacheless()` so this read gets a **new** empty
debounce rather than the long-lived Runtime’s stale HEAD.

For each configured locale (`eveli.metis.search.locales`, default `en, fi, sv`)
it runs the article program and asks `SiteSearchDocumentBuilder` for documents.
A locale that is configured but missing from the bundle is logged and skipped.
If **none** of the locales resolve, the job fails with
`ContentUnavailableException` and does not touch rows.

The limaone world hash of that view is stored on the job as `bundle_hash`. A
completed job for a publication only counts as “already indexed” when that
hash still matches cacheless HEAD. A read that still saw the previous bundle
cannot permanently skip the live publication.

There must be a **live** publication. Authoring import does not fire a deploy
event and does not start a reindex. “Live” means `startsAt` is null or not in
the future (`LivePublications.isLive`). Among live publications the latest
`startsAt` wins, then `createdAt`.

A publication with a future `liveDate` is indexed when it becomes live, not
when created. Immediate publishes still start a job right away.

---

## Metadata and embeddings

### Chat metadata

For each document the chat model (`llama3.2` locally) is asked for
citizen-facing helpers in that locale: a short description (under 120 words),
5–10 synonyms, 5–10 related phrases. The prompt is in `MetadataGenerator`.
Output is stored as `ai_metadata` and folded into:

- the embedding input (`searchText + "\n" + metadata`)
- FTS band **C** (low weight)

The prompt always asks for the locale’s language, so Finnish documents get
Finnish helpers. The LLM context handed in is title, description, and topic
names, truncated to `indexing.max-llm-context-chars`. Page body is **not** in
the LLM prompt (it is already in `searchText` / band D).

If the chat call fails after two attempts, the document still indexes with
description-only metadata. A missing description does not fail the document.

Bump `indexing.metadata-prompt-version` when the prompt changes. The version
is part of the content hash, so a normal reindex rewrites every row.

### Embeddings

The embedding model is `bge-m3` (1024 dimensions). Cosine distance. The column
is `VECTOR(1024)` in the migration; `indexing.embedding-dimension` only
**checks** that the column is still that width. A mismatch fails the job.
Changing width is a new migration, not a property change.

Index-time `embed()` is **not** gated by the query-embed semaphore. The
indexing pool (`indexing.concurrency`, default 1) already limits how many
documents run at once. One document’s timeout
(`indexing.document-timeout-seconds`, default 600) starts when that document
starts, not when it was queued.

### Content hash (skip unchanged rows)

SHA-256 of:

- locale
- owning topic id
- title
- search text
- embedding model id (`spring.ai.ollama.embedding.options.model`)
- `indexing.metadata-prompt-version`

Unchanged hashes are skipped on reindex. Switching the embedding model id, or
bumping the prompt version, already invalidates stored hashes, so a normal
reindex reprocesses those rows. `force=true` ignores hashes.

`ai_metadata` is **not** in the hash. The hash is computed *before* the chat
call, from the deterministic document. That is why the prompt version is in
the hash: it is the stand-in for “the helpers might have changed.”

---

## Store

Flyway location `classpath:db/metis/search`, appended only while
`eveli.metis.search.enabled` is true. Site search owns version band **V4_x**
(`V4_1__metis_search.sql`). Later capabilities take V5_x+. A database that
never enables search never needs pgvector.

Needs extensions `vector`, `pg_trgm`, `unaccent`. The application role must be
allowed to `CREATE EXTENSION`, or a DBA pre-creates all three (typical on
Cloud SQL).

### `metis_search_index`

| Column | Role |
| --- | --- |
| `workflow_id` + `locale` | Unique key. One document per service per language. |
| `topic_id` | Owning topic, for portal mapping. |
| `title` | Workflow name. FTS weight **A**. |
| `category` | First owning topic name. |
| `search_text` | Concatenated title / description / supplemental. Also used by the trigram fallback. |
| `ai_metadata` | LLM helpers. FTS weight **C**. |
| `search_vector` | Weighted `tsvector`: title **A**, description **B**, `ai_metadata` **C**, supplemental **D** (`setweight` / `to_tsvector`). |
| `embedding` | `VECTOR(1024)`, HNSW cosine index. |
| `content_hash` | Skip-if-unchanged. |
| `indexed_at` | Last upsert. |

Indexes: GIN on `search_vector`, HNSW on `embedding`, GIN trigram on
`search_text`. Stemmer follows locale: `finnish` / `swedish` / `english`, else
`simple`. Only `en`, `fi` and `sv` have PostgreSQL stemmers.

FTS weights mean a title match outranks a synonym in `ai_metadata`, which
outranks a heading buried in page text.

### `metis_search_reindex_job`

| Column | Role |
| --- | --- |
| `status` | `RUNNING`, `CANCELLING`, `CANCELLED`, `COMPLETED`, `FAILED`. |
| Unique partial index on `(true) WHERE status IN ('RUNNING','CANCELLING')` | Single-flight claim across replicas. |
| `publication_id` | Live publication this job was started for. |
| `bundle_hash` | Cacheless HEAD the job indexed. |
| `embedding_model` | Model id at start, visible on status. |
| `total_count` / `processed_count` / `skipped_count` | Progress. |
| `last_progress_at` | Heartbeat. Abandoned-job reclaim uses this, not `started_at`. |
| `restart_after_cancel` / `restart_force` | Replacement reindex after cancel. |

---

## Reindex

```
trigger (boot / deploy event / 60s reconciler / POST …/reindex)
        │
        ▼
MetisLiveIndexTrigger     stamps live publication id
        │
        ▼
tryStart                  INSERT RUNNING (unique index = one job in the cluster)
        │
        ▼
IndexingService.runReindex
   ├── cacheless limaone read
   ├── skip unchanged content_hash
   ├── per document: chat metadata → embed → upsert
   ├── on full success: deleteStaleRows
   └── COMPLETED  (or FAILED / CANCELLED, previous index kept)
```

Only one job runs across all app replicas. `tryStart` inserts a `RUNNING` row;
a second insert hits the unique index and the caller gets HTTP 409 with the
current job (`accepted: false`).

Entry points all go through `MetisLiveIndexTrigger` so the live publication id
is stamped on the job:

| Trigger | When |
| --- | --- |
| `auto-reindex-on-startup` | Boot, only if the index is empty |
| `ContentDeployedEvent` | A live publication record was created (`reindex-on-deployment`) |
| `MetisLivePublicationReconciler` | Every `live-publication-check-seconds` (60), for a `liveDate` that just became now, and as catch-up after a restart that missed the instant |
| `POST …/reindex` | Manual. `force=true` rewrites hashes. `replace=true` asks the current job to stop, then starts another |

Job states: `NONE`, `RUNNING`, `CANCELLING`, `CANCELLED`, `COMPLETED`,
`FAILED`. Portal search is ready only after a job has **`COMPLETED`**. An empty
table, a failure, or a cancel leaves keyword search up until the next
completed job.

`deleteStaleRows` runs only on a complete success, so a partial failure does
not wipe the previous index. Treat `COMPLETED` as done only when
`processedCount + skippedCount` equals `totalCount`. An incomplete run
(`processed + skipped < total`) used to be marked completed when queued
documents hit the timeout together; that path now **fails** the job and leaves
the previous index in place.

Cancel: in-flight work is at most `indexing.concurrency` documents; the rest
stop at the next `isRunning` check. `replace=true` sets `restart_after_cancel`
and starts the next job when the claim is released.
`POST …/reindex/cancel` stops without starting another.

Abandoned jobs (no progress for `indexing.abandoned-after-seconds`, default
3600, from **last progress**, not start) are marked failed so another instance
can claim.

On JVM shutdown, `MetisSearchClientImpl.shutdown()` marks the running job
failed so the unique index is released. The reindex executor is a single
**non-daemon** thread (a daemon would die on JVM exit and leave `RUNNING`).
It is a plain `ExecutorService` so it does not steal Spring `@Async`
resolution.

---

## Query ranking

`HybridSearchService.rankedResults` always tries three things, then fuses.
Candidate lists are `max(limit * 3, 30)` rows; the caller then gets `limit`.

### 1. Primary FTS

`websearch_to_tsquery` on `search_vector` (title-weighted). This is the
“did the citizen type words that are actually in the document?” list.

### 2. FTS list for fusion

If primary already has `min-results-before-fallback` hits (default 1), keep
that list so weaker keyword hits still break ties. If primary is empty, a
stemmed-OR plus trigram `word_similarity` fallback runs. That fallback is a
**sequential scan**: `unaccent()` plus `word_similarity >=` cannot use the GIN
index. Threshold is `query.trgm-threshold` (0.2). It exists for typos and
compound Finnish words, not as a second semantic path.

### 3. Vector

Embed the query (`bge-m3`), cosine k-NN, then:

- drop the whole list if the best score is below `min-vector-score` (0.30)
- drop rows below `score-drop-off-ratio` × best (0.90)

The drop-off is **not** applied to a keyword-only list, so a form-name hit
stays even when vector neighbours would have been cut.

### Fusion (RRF)

Fusion is **reciprocal rank fusion**, not raw score mixing. Cosine 0.81 and
`ts_rank` 0.4 are not comparable; ranks are.

Each hit gets `weight / (k + rank)` with `k = query.rrf-k` (60). Vector list
uses `vector-weight` 0.8; FTS list uses `fts-weight` 0.2. A document in both
lists gets both contributions. Primary FTS hits that the vector list missed
are still inserted so a strong title match is not lost.

Worked example, `k = 60`. Vector rank 1 of service A:
`0.8 / 61 ≈ 0.0131`. FTS rank 1 of the same service: `0.2 / 61 ≈ 0.0033`.
Fused ≈ 0.0164. Vector rank 2 of service B: `0.8 / 62 ≈ 0.0129`. A is first
because it appeared in both lists, not because its cosine was higher.

The FTS list used for *scoring* is the fusion list (primary or trigram).
Membership of new ids from FTS is **only** from primary FTS. Trigram-only
hits are not unioned into a vector list that already survived 0.45: the
vector path already decided this is a semantic query.

### Two empty-primary cases

These must not be mixed.

**Embed unavailable** (timeout, Ollama busy, `embed-concurrency` reject).
Hybrid returns **keyword ranking**, including the trigram fallback if primary
FTS was empty. The citizen still gets something. `fallback` stays false.

**Embed succeeded**, primary FTS empty, best vector score below
`min-vector-score-without-keyword` (**0.45**). Return **empty**. Gibberish
(`qwerty`) lives in the 0.30–0.45 band. Showing random services is worse than
showing nothing. This path does **not** fall through to trigram.

Do not lower 0.45 to recover a weak natural-language query whose metadata
lacks the user’s words. Bump `metadata-prompt-version` and reindex so the LLM
writes that language into band C.

### Query embed concurrency

`EmbeddingService.embedQuery` takes a permit from
`query.embed-concurrency` (default: same as `query.concurrency`, **4**). Cache
of the last 1 000 finished query vectors is checked first. Identical
**in-flight** strings join the same `CompletableFuture` and do not take a
second permit.

Production overlapping unique searches all call Ollama. `bge-m3` still runs
one forward at a time on a single Ollama process; extra HTTP calls queue
there. On GPU that queue still finishes inside the ~3 s embed timeout (search
budget minus two seconds). Local `application-dev.yml` sets
`embed-concurrency: 1` so CPU Ollama does not pile unique queries until they
all miss the timeout. Overflow of a *different* uncached string is FTS-only,
not `fallback: true`.

Index-time `embed()` does not use this semaphore.

---

## Concurrency and threading

| Pool | Size | Role |
| --- | --- | --- |
| Search executor | `query.concurrency` (4), queue `query.queue-capacity` (50), `AbortPolicy` | Hybrid search. Full queue → `fallback: true`. Timeout `query.timeout-seconds` (5). |
| Query-embed semaphore | `query.embed-concurrency` (default 4, local 1) | How many unique uncached query embeds may call Ollama at once. |
| Indexing executor | `indexing.concurrency` (default 1) | Documents in parallel during reindex. |
| Reindex executor | 1 non-daemon thread | Owns the job; must not block on Thena SQL. |
| Vert.x worker pool | (existing) | `MetisSearchSql.await` and live-publication resolution. Never on the event loop, never on the single reindex thread. |

Awaiting Thena SQL on the Vert.x event loop deadlocks. Live-publication
resolution in `MetisLiveIndexTrigger` is emitted onto the worker pool for the
same reason.

---

## Worker API and UI

| Method | Path | Role |
| --- | --- | --- |
| GET | `/worker/rest/api/metis/status` | `ROLE_Authorized` |
| GET | `/worker/rest/api/metis/search/status` | `ROLE_Authorized` |
| POST | `/worker/rest/api/metis/search/reindex` | `ROLE_ASSET_ADMIN` |
| POST | `/worker/rest/api/metis/search/reindex?replace=true` | `ROLE_ASSET_ADMIN` |
| POST | `/worker/rest/api/metis/search/reindex/cancel` | `ROLE_ASSET_ADMIN` |

There is no blanket `/metis/**` permission. A new capability is unreachable
until it is granted a role. `PropertyAuthorizationTest` asserts that.

A 409 on reindex means another job is in flight; the body is the current
status. Poll `GET …/search/status` until the state is no longer `RUNNING` or
`CANCELLING`.

The worker Metis page is chrome: `metis` in `eveli.tenant-features` (or the
user profile). It does not start the platform. Without `eveli.metis.enabled`
the page still opens but reports that Metis is off.

Platform status (`GET /metis/status`) reports provider, model ids, and one
entry per capability (`READY` / `NOT_READY` / `DISABLED` / `ERROR`). Anything
more detailed belongs on the capability’s own status endpoint.

---

## Class map

| Class | Responsibility |
| --- | --- |
| `SiteContentReader` | Cacheless limaone read, anonymous participant, per-locale article program |
| `SiteSearchDocumentBuilder` | One document per workflow × locale, generic-topic filter, content hash |
| `MarkdownStripper` | Page markdown → plain text |
| `MetadataGenerator` | Chat prompt → `ai_metadata` |
| `IndexingService` | Skip hashes, process documents, upsert, delete stale, fail incomplete |
| `ReindexJobService` | Claim, cancel, heartbeat, abandoned reclaim, portal-ready |
| `MetisSearchIndexTable` | Upsert with `setweight` bands, k-NN, FTS primary/fallback |
| `FtsSearchService` | Primary `websearch_to_tsquery`, trigram fallback |
| `VectorSearchService` | Query embed, k-NN, 0.30 floor, 0.90 drop-off |
| `HybridSearchService` | RRF, empty-primary 0.45, keyword-only when embed fails |
| `EmbeddingService` | Index `embed()`, query `embedQuery()` with cache, coalesce, semaphore |
| `MetisLiveIndexTrigger` | All entry points; stamps `publication_id` |
| `MetisLivePublicationReconciler` | 60 s scheduled-publication catch-up |
| `MetisReindexListener` | `ContentDeployedEvent` |
| `LivePublications` | Which deployment is live right now |
| `GamutSiteSearchController` | Public GET, rate limit, fallback |
| `MetisSearchApiController` | Worker status / reindex / cancel |
| `useBackendSearch` | Debounce, abort, 404 → unavailable |
| `useSemanticResults` | Id mapping, pending empty, all-unmapped → keyword |

---

## Measured results

Live municipal index, a few hundred documents, 11 Sep 2026, live
`GET /portal/site/search`. Not CI. Query sets are kept locally and are not in
this repository.

Finnish set of 20 citizen-worded queries:

| Metric | Result |
| --- | --- |
| Correct answer ranked first | 20 / 20 |
| Correct answer in top 3 | 20 / 20 |
| Correct answer in top 5 | 20 / 20 |
| Mean rank when found | 1.00 |
| Results returned per query | 2.0 mean, 1 to 8 |
| Fallback responses | 0 |
| Cold latency (p50) | ~200 ms |
| Warm latency (p50) | ~30 ms |

Latency is from an uncached pass on the same host. The 20/20 run was after
query embeddings were already cached (~38 ms p50). Do not report 38 ms as
cold.

Two known misses on an older Finnish set: one paraphrased query with no
keyword overlap scored ~0.39 and returned empty (below 0.45); another ranked
the intended service 5th behind a related one. On that set hybrid was 18/20
first (19/20 if the 0.45 floor is ignored). Keyword search on the same 20 was
9/20.

English and Swedish sets of 7 queries each scored 7/7 first. Two Swedish
soft spots ranked 2nd (a related service ranked above the intended one).

---

## Limits

- Single tenant: documents carry no `cockpit_id`.
- Only `en`, `fi` and `sv` have PostgreSQL stemmers; other locales use `simple`.
- Metadata generation on CPU is tens of seconds to minutes per document. A few
  hundred documents against CPU Ollama takes hours.
- Disabled, `inHouse`, and non-dev `devMode` workflows never reach the site
  JSON, so they are not indexed. Login-required services **are** indexed;
  opening the form is still gated.
- Gibberish with no keyword hit is rejected below
  `query.min-vector-score-without-keyword` (0.45). Do not lower that floor to
  recover weak natural-language queries; bump
  `indexing.metadata-prompt-version` and reindex instead.
- Trigram FTS fallback is a sequential scan. It runs only when primary FTS is
  empty, and its hits are not unioned into a surviving vector list.
