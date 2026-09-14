# Senior review: Metis site search

Talking points for a codebase-owner review. This is a **defense** document: every
choice below is something we would keep unless the owner makes a product call
to change it.

It is written for someone who has not lived in this code. Ranking and the
document model are in [IMPLEMENTATION.md](IMPLEMENTATION.md). How to turn it on
is in the [Metis README, Site search](../../README.md#site-search).

**Verdict:** The architecture fits Digiexpress. Platform vs capability, Thena SQL
annotations, Application `@Import`, path-based permissions, and the portal
degrade path are consistent with how Gamut, Assets and Batches are wired. The
reindex single-flight, content-hash skip, cacheless bundle read, and
publication/bundle-hash reconciler are the strongest parts. If the review opens
on “this looks like a lot of new code,” that volume is a new platform plus one
capability, not a one-off search feature bolted onto eveli-client.

---

## What you are looking at

Digiexpress is a municipal case-management platform. Staff use the **worker**
UI (Eveli). Citizens use the **portal** (Gamut). Content authors publish a
**bundle**: pages, topics, and **workflow links** (the buttons that open a form).
A **publication** is a named, versioned snapshot of that bundle. It can go live
immediately or at a future `liveDate`.

**Metis** is the AI platform for Digiexpress. Site search is the first
**capability** on that platform: semantic search over the live published site.
A citizen describes a situation in everyday language and should land on the
right service even if those words are not in the page title.

Two stores sit on every indexed document:

- **Full-text (FTS):** PostgreSQL `tsvector`. Keyword matching with language
  stemmers (`finnish`, `english`, `swedish`).
- **Vector:** pgvector. The embedding model (`bge-m3`, 1024 numbers) places
  similar *meanings* near each other, even when the words differ.

At query time those two ranked lists are fused with **reciprocal rank fusion
(RRF)**. If anything on the backend path is not ready, the portal does not
show an error: it keeps the keyword search it already had.

| Word in this document | Meaning |
| --- | --- |
| **eveli-client** | Spring host: REST, Flyway, permissions, auto-config. Apps `@Import` it. |
| **metis-client** | Library: search, index, SQL, ranking. No Spring MVC. |
| **limaone** | Content compiler. The article program is the site JSON the portal already ships. |
| **Thena** | Our SQL toolkit. Annotations generate typed queries against a Vert.x `Pool`. |
| **Flyway** | Schema migrations. One history table for the whole app. |
| **Gamut** | Citizen portal. |
| **Publication / deployment** | A published content snapshot. “Live” means its start time is now or in the past. |
| **Reindex** | Rebuild or incrementally update `metis_search_index` from the live bundle. |
| **`fallback: true`** | Backend tells the portal “use client-side keyword search.” Not an HTTP error. |

---

## How a search actually happens

1. The citizen types in the portal search box.
2. After 350 ms of no typing, the browser calls `GET /portal/site/search?q=…&locale=fi`.
3. If the latest reindex job is not `COMPLETED` (including “no job has ever
   run”), the API returns `{ fallback: true, results: [] }` and the portal
   keeps keyword search.
4. Otherwise Metis runs hybrid search: keyword FTS, then embed the query, then
   nearest-neighbour in pgvector, then RRF.
5. Hits are `{ workflowId, topicId, title, score, … }`. The portal maps those
   **ids** onto the site JSON it already has. Display names are never used as
   a join key.
6. Forms and topics in the result list are replaced by that ranking. Phone
   numbers and hyperlinks stay client-side on purpose.

A **reindex** is a background job: read the live limaone bundle, skip
unchanged documents by content hash, ask the chat model for citizen phrasing,
embed, upsert, delete rows that no longer exist. Only one job may run in the
whole cluster (database unique index). The portal stays on keyword search
until that job reaches `COMPLETED`.

---

## Open with this

Say these first if the review opens on volume. Each item is already in the
code; this section is what it *means*.

### Capability layout is copy-pasteable

`metis-parent/README.md` has a section “Adding a capability.” Feedback analysis
is the next one (`V5_x`). The layout is `api` / `spi` under
`io.resys.metis.<capability>`, a flag `eveli.metis.<capability>.enabled`, a
Flyway location `db/metis/<capability>`, and an `EveliAutoConfigMetis*` class
that the Application `@Import`s. Site search is the template, not a one-off.

### Permissions are least-privilege

`eveliPermissions.yaml` lists exact paths. There is **no**
`/worker/rest/api/metis/**` rule. A future `/metis/feedback/**` is denied until
someone adds a line. `PropertyAuthorizationTest` hits
`/worker/rest/api/metis/feedback/status` and asserts both `ROLE_Authorized` and
`ROLE_ASSET_ADMIN` are denied.

What *is* granted:

- `GET /metis/status` and `GET /metis/search/**` — any worker (`ROLE_Authorized`)
- `POST /metis/search/reindex` and `…/cancel` — `ROLE_ASSET_ADMIN` only

The worker sidebar is a separate switch (`metis` in `eveli.tenant-features`)
plus `NAV_TO_METIS` (`RELEASE_VIEW` / `RELEASE_EDIT`). Seeing the page is not
the same as being allowed to rebuild the index.

### Flyway `V4_1` is the protocol

`db/metis/search/V4_1__metis_search.sql` is the runtime schema. Comments in
that file are the contract, not leftover notes:

- `CREATE EXTENSION` for `vector`, `pg_trgm`, `unaccent`
- GIN on `search_vector` (keyword)
- HNSW on `embedding` (nearest neighbour)
- GIN trigram on `search_text` (fuzzy fallback)
- `publication_id` and `bundle_hash` on the job row (which publication, which
  content snapshot)
- `last_progress_at` (heartbeat for abandoned-job reclaim)
- partial unique index `WHERE status IN ('RUNNING','CANCELLING')` — at most
  one in-flight job in the whole database

Site search owns the `V4_x` version band. The next capability takes `V5_x`.
Never put a non-search migration in `V4_x`.

### Content hash includes the model and the prompt version

Unchanged documents are skipped on reindex. The hash is not “file bytes.” It
is locale, owning topic id, title, assembled search text, **embedding model
id**, and **`metadata-prompt-version`**. Changing the model or bumping the
prompt version makes every hash miss, so a normal reindex (not `force=true`)
re-embeds. That is how we retune metadata without a separate “wipe” button.

### Cacheless read

**The problem.** limaone compiles the published site into a **bundle** (article
program, workflows, forms). Compiling is a database read plus a lot of CPU, so
the process keeps one long-lived `Runtime` and reuses the last compiled bundle
instead of hitting the DB on every portal request.

That reuse is a **debounce**, not a hash map of every version. `Bundle_Lazy`
remembers “I already loaded this tenant” in a Caffeine cache that expires after
a fixed number of seconds. While the key is still there, `await()` returns the
previously compiled programs and does **not** look at the database. After expiry
the next access reads the world hash and recompiles if it changed.

There are two views:

| Call | Debounce | Who uses it |
| --- | --- | --- |
| `runtime.getBundle()` | **30 s** | Portal / worker serving the live site. A few seconds of stale content after a publish is acceptable. |
| `runtime.getCachelessBundle()` | **5 s** | Places that need a fresher view (Metis, some asset controllers). The Runtime **keeps** this instance, so the 5 s clock is shared. |

**Why 5 s is still not enough for indexing.** A publication can go live and
Metis can start a reindex in the same second. If the indexer used
`getBundle()`, it could compile the **previous** site for up to 30 s and write
the old documents into the search table (or skip, thinking nothing changed).
`getCachelessBundle()` is better, but that object is the same one everyone
else is using: if something called it two seconds ago, three seconds of the
old world remain.

**What the indexer does.** `SiteContentReader` takes the cacheless bundle and
calls `withCacheless()` **again**. That constructs a **new** `Bundle_Lazy` with
an empty debounce map. The first query therefore misses the cache and reads
the database immediately. It still uses the 5 s constructor (limaone has no
“zero debounce” API); the point is that *this job* does not inherit a warm
5 s window from the shared Runtime instance.

The unit test `readsTheCachelessBundleSoThatADeploymentIsNotIndexedFromTheOldSite`
asserts the reader called `getCachelessBundle().withCacheless()` and never
`getBundle()`.

**What this does not fix.** A brand-new DB read can still see the previous
HEAD if limaone has not updated the world ref yet. That is why job rows store
`bundle_hash`: if we indexed hash A and live HEAD is now B, the reconciler
does not treat the publication as done and starts another job. Cacheless is
the “don’t wait 30 s” path; `bundle_hash` is the “don’t permanently skip”
path.

### Weighted tsvector bands A–D

On upsert, four text fields are concatenated into one `tsvector` with
PostgreSQL weights:

| Band | Weight | Content |
| --- | --- | --- |
| A | highest | Title |
| B | | Description |
| C | | LLM metadata (synonyms, related phrases) |
| D | lowest | Supplemental topic/page text |

A title match outranks a mention buried in a listing page. This is ordinary
PostgreSQL full-text ranking, not a custom scorer.

### Portal mapping by id, `searchOnly` preferred

The backend never returns a renderable card. It returns ids. `backend-results.ts`
looks up `workflowId` in the site JSON. Some workflows appear under more than
one topic; a synthetic **search-only** topic is preferred as the owner so the
citizen lands in a sensible place.

If **every** hit fails to map (stale index vs a site that just changed), the
hook returns “unavailable” and the portal falls back to keyword. Partial
unmapped hits are skipped; the rest still show.

### Abort, debounce, and `answered.query === query`

The portal waits 350 ms after the last keystroke, then fetches. A new
keystroke **aborts** the in-flight request (`AbortController`). The UI treats
results as ready only when `answered.query === query`. That stops a slow
response for “dog” from overwriting the list for “dog licence.”

Client timeout is 8 s; server timeout is 5 s. A late success is kept rather
than discarded.

### `LivePublications` is shared

“Is this publication live?” is a pure function (null start time, or start time
not in the future; latest start wins, then latest created). The publication
controller and Metis both call it. Tests cover the function. “What is live”
is not duplicated in two if-statements.

### Integration tests hit production Flyway

`MetisSearchIntegrationTest` starts `pgvector/pgvector:pg17` and migrates
`classpath:db/metis/search` — the same files production uses. Covered:

- index, skip by hash, delete stale rows
- partial failure keeps old rows (no `deleteStale` on failure)
- cancel, replace, single-flight
- portal fallback while a job is running **and** when no job has completed
- publication id + bundle hash so a stale HEAD cannot skip the live publication

Hybrid unit tests separate “embed failed” from “embed succeeded but weak.”

### Docs state the limits in public

Single tenant, stemmers only for `en`/`fi`/`sv`, CPU cost of metadata, the
global blast radius of `*:missing`, and “do not lower the 0.45 floor” are in
README / IMPLEMENTATION, not tribal knowledge.

---

## Likely questions

### Why is this a new Maven parent (`metis-parent`) instead of eveli-client?

**What the owner is asking:** Why a new library, new package `io.resys.metis`,
new BOM entry — instead of classes under `io.digiexpress.eveli.client`?

**How Digiexpress already works:** limaone, thena, eveli-permissions are
libraries with `api`/`spi`. `eveli-client` hosts them: Spring beans, REST,
Flyway, JWT. Apps (`eveli-app`, `eveli-app-gcloud`) `@Import` auto-config
classes. They do not contain the product rules.

**What we did:** `metis-client` is that library. `eveli-client` depends on it
and owns `EveliAutoConfigMetis*`, the controllers, and `db/metis/search`.
The old `ai-parent/README.md` was leftover documentation, not a module that
was deleted from the build.

**Why not put it in eveli-client:** The next capability (feedback) would then
live in the host too, and the “how to add a capability” story collapses.
Search rules (generic-topic threshold, anonymous participant, RRF weights)
belong with the capability, not with servlet wiring.

---

### Why three switches (`eveli.metis.enabled`, `eveli.metis.search.enabled`, tenant feature `metis`)?

**What the owner is asking:** This looks over-flagged. Why not one `metis=true`?

**What each switch does:**

| Switch | Layer | Effect |
| --- | --- | --- |
| `eveli.metis.enabled` | Platform process | Spring AI models, `spi.ai` wrappers, `GET /worker/rest/api/metis/status`. Valid **alone**: models up, no search. |
| `eveli.metis.search.enabled` | This capability | Search beans, portal endpoint, Flyway location `db/metis/search`. Requires the platform flag or boot fails. |
| `eveli.tenant-features` contains `metis` | Worker UI | Sidebar item and `/secured/$locale/worker/metis/`. Does **not** start beans. Without the platform flag the page still opens and says Metis is off. |

**Why not one flag:** Batches already separate “feature visible in the UI”
from “beans exist.” Feedback will need `eveli.metis.feedback.enabled` without
dragging site-search Flyway, Ollama embeddings, or the portal endpoint with
it. The tenant feature is per-environment chrome (some tenants never see the
page). The two process flags are how you deploy “models only” vs “models +
search.”

**Hard check:** `EveliAutoConfigMetisFlyway` throws if search is on and the
platform is off. Search without models is not a supported half-state.

---

### Why is Flyway opt-in, and why `ignore-migration-patterns: "*:missing"`?

**What Flyway is doing:** On boot, Flyway applies SQL files whose versions are
not yet in `flyway_schema_history`, then **validates** that every *applied*
version still has a matching file on the classpath (`validate-on-migrate:
true`). If an applied file is gone, the app refuses to start.

**Why the location is opt-in:** `V4_1` does `CREATE EXTENSION vector`. A
database that will never run Metis must not need pgvector. The customizer
appends `classpath:db/metis/search` only while `eveli.metis.search.enabled`
is true.

**The trap:** Once `V4_1` has been applied, turning the flag **off** removes
the location. Flyway still sees `V4_1` in history, cannot find the file, and
with validate-on-migrate would not boot. Tables would still exist; the
process would just die.

**`*:missing`:** Flyway setting meaning “if an applied migration’s file is
missing, ignore that validation error.” Tables stay. The app boots.

**The cost, said in public:** The pattern is `type:state`, not “only V4.” A
deleted, already-applied **core** `V3_x` would also be ignored. That is on
the customizer javadoc, in YAML, in `README_CONFIG_PROPERTIES.md`, and in the
site-search README.

**Alternatives and why we did not take them:**

| Alternative | Why not |
| --- | --- |
| Always keep `V4_x` on the classpath | Every environment needs pgvector, including those that never enable Metis. |
| Separate Flyway history table per capability | New operational surface. The rest of eveli uses one table. |
| `CREATE EXTENSION` in a core `V3_x` | Same pgvector requirement for everyone, and Cloud SQL often cannot `CREATE EXTENSION` as the app role. |

**If the owner hates `*:missing`:** The honest fallback is “once enabled, the
location stays on the classpath forever.” Simpler validate behaviour; you give
up “this database never needs pgvector.”

---

### Why are tables declared twice (Flyway and `@TenantSql.Table`)?

**Thena:** Queries are not hand-written JDBC. You annotate an interface
(`@TenantSql.Table`, `@TenantSql.Find`). An annotation processor generates
`MetisSearchDb` with typed methods. That processor needs a `CREATE TABLE` in
the annotation so it knows column names.

**Flyway:** What actually runs in production: extensions, GIN, HNSW, trgm,
the partial unique index, comments. Integration tests migrate the Flyway
location. They do **not** create tables from the annotation.

**Why the annotation DDL is thinner:** Indexes and extensions are Flyway’s.
If we duplicated HNSW in the annotation, tests that accidentally used the
annotation path would drift from production. Other Thena clients already
generate SQL from annotations while the host owns the real schema.

**Sound-bite:** The incomplete annotation is not the runtime schema. `V4_1`
is.

---

### Why `StructureType.unknown` and no `cockpit_id`?

**Thena tenants:** Many Thena modules are multi-tenant. A `cockpit_id` (or
equivalent) on every row would let several customers share one database.
`StructureType` tells Thena which physical layout the tenant uses (`doc`,
`git`, `grim`, …). `unknown` means “this is not one of those layouts.”

**What we did:** Site search is **single-tenant on purpose**. Documents have
no tenant column. One municipal deployment, one index.

**Why not add the column “for later”:** Speculative schema. The next
capability is `V5_x` and independent. Multi-tenant search would be a new
migration on this band when a real customer needs it, not a reason to block
V4.

---

### Why does `metis-client` depend on limaone and Spring AI?

**limaone:** The documents **are** limaone article-program output — the same
site JSON the portal already renders. `SiteContentReader` runs that program
with a dummy anonymous participant, per locale, and builds one document per
workflow link.

If that reader lived in eveli-client, `metis-client` would be a generic
vector store, and the product rules would sit in the host:

- drop body text from topics that link to more workflows than
  `generic-topic-threshold` (those are listing pages, not the service)
- anonymous participant (same visibility as the public site JSON)
- cacheless read (see above)

Those rules belong with the capability. Feedback will not import
`SiteContentReader`; it will import `spi.ai`.

**Spring AI:** Wrapped once in `spi.ai` (`EmbeddingService`,
`StructuredChatService`). Capabilities take those types, not
`EmbeddingModel` / `ChatClient`. Constructors still mention Spring AI because
the wrappers are thin — same kind of host coupling as Thena `Pool` on a
thena client.

---

### Why block on `Uni` (`MetisSearchSql.await`) instead of staying reactive?

**This is not a Metis invention and does not break Thena.** Digiexpress already
has two normal ways to use Mutiny + Vert.x + Thena. Metis uses the second.
The comments exist so a reader who sees `await()` in a helper does not think
we abandoned Uni.

**The usual path (pass the `Uni` through).** A Thena call *is* a `Uni`.
Controllers like `TaskApiController` return that handle unchanged:
`return taskClient.queryTasks().getOneById(id)`. Spring subscribes, Vert.x
runs the SQL, the event loop delivers the row, the HTTP response is written.
No thread sits parked. That is what “staying reactive” looks like, and it is
what we do when one query is the whole request.

**The other usual path (hop, then `await`).** Sometimes the work is sequential
Java, or it calls a blocking library. Then the code moves onto a **worker**
thread and parks there until the Thena `Uni` completes. limaone already does
exactly this (`DialobProgramImpl`, `FlowTaskProgramImpl`:
`runSubscriptionOn(workerPool).await()`). So do boot, schedulers, and some
controllers (`EveliAutoConfig`, `DialobScheduler`, `AttachmentApiController`).
`MetisSearchSql.await` is that same `uni.await().atMost(30s)`, named once.

Search takes this second path because a query is not one SQL handle: FTS,
then a **blocking** Spring AI `embed()`, then vector SQL, then fuse in Java.
Reindex is hundreds of those steps. Composing that as a pure `Uni` chain
would be a rewrite, not more “correct” Thena. Spring AI is synchronous either
way.

**The only Thena rule:** Vert.x has a tiny **event loop**. Those threads start
I/O and react when it finishes; they must not sleep. PostgreSQL answers are
delivered **on that loop**. If the loop itself called `await()`, it would
sleep waiting for a completion only it can run → hang the whole Thena pool.
Parking on a worker is fine: the loop stays free to deliver.

That deadlock is why `blocking()` and `MetisLiveIndexTrigger` hop with
`runSubscriptionOn` / `emitOn` *before* any `await()`. Same hop limaone uses.
We did not add a new concurrency model.

| Call | Where the `await()` runs | Why that pool |
| --- | --- | --- |
| Portal `findByText` | `metis-search-query-` | Bounded; a full queue fails the `Uni` and the portal falls back. Dedicated because embed is slow and must not share limaone’s general worker pool with unrelated work. |
| `getIndexStatus` / `startReindex` / `cancel` | Vert.x **default worker pool** (`blocking()`) | Short SQL. Must **not** use the reindex thread: that one can be busy for hours, and a status poll would queue behind it. |
| Reindex body (`runReindex`) | `metis-search-reindex-` (documents on `metis-search-indexing-`) | Hours of LLM/embed. Own thread so it cannot steal query workers or Spring `@Async`. |
| Live-publication resolve | same default worker pool | Authoring SQL is also Thena. |

What *is* Metis-specific is the **dedicated query/reindex pools** (next
question), not the fact that we `await`. Do not volunteer a reactive rewrite
of hybrid search. If asked, the answer is: controllers still return `Uni`;
inside we use the limaone hop-then-await pattern because embed is blocking.

---

### Why dedicated thread pools?

Three pools, three jobs:

| Pool | Size | Why it exists |
| --- | --- | --- |
| `metis-search-reindex-` | 1, **non-daemon** | One reindex at a time **in this process**. A daemon thread dies when the JVM exits and would leave the DB row `RUNNING` until abandon timeout. |
| `metis-search-indexing-` | `indexing.concurrency` (default 1) | Parallel LLM + embed per document. Default 1 because local Ollama is CPU-bound. |
| `metis-search-query-` | bounded queue, `AbortPolicy`; embed permits default = pool size | Citizen queries. Full queue **rejects** → `fallback: true`. Embed cap (`embed-concurrency`) is how many unique query vectors may be in Ollama’s queue; overflow of a *different* string is FTS-only. |

**Two layers of single-flight:** The DB partial unique index is the
**cross-replica** guard (two Cloud Run instances cannot both run a job). The
reindex executor is the **in-process** guard (this JVM does not overlap jobs
even without the DB). Both are required.

---

### Why a DB partial unique index instead of ShedLock / `@SchedulerLock`?

**ShedLock** (and Spring `@SchedulerLock`) is a library lock: “only one
scheduled method runs.” It does not know about `CANCELLING`, cancel-and-
replace, or “this replica died mid-job.”

**What we need:** At most one in-flight job in the **database**, including
the window where the operator asked to stop but the current document is still
in the LLM. A second replica must not start during that window.

**The whole protocol is one index:**

```sql
CREATE UNIQUE INDEX idx_metis_search_reindex_job_inflight
    ON metis_search_reindex_job ((true))
    WHERE status IN ('RUNNING', 'CANCELLING');
```

`(true)` is a constant. In PostgreSQL a unique index on a constant means “at
most one row matching the WHERE.” Any second `RUNNING`/`CANCELLING` insert
fails.

**`tryStart`:** `INSERT … SELECT … WHERE NOT EXISTS (in-flight row) RETURNING id`.
If a concurrent insert still races the unique index, duplicate is recovered
as “not started” (`Optional.empty()`), not as a 500.

**Abandoned jobs:** Measured from **`last_progress_at`**, not `started_at`.
A healthy three-hour CPU Ollama run keeps heartbeating as documents finish.
A crashed replica stops heartbeating; after `abandonedAfterSeconds` another
instance marks it `FAILED` and can start.

**Last writer is constrained:**

- `markCompleted` requires `status = 'RUNNING'` — a completed row is not
  overwritten by a late complete.
- `shutdown()` / `markFailed` require `RUNNING` or `CANCELLING` — a job that
  already completed is not flipped to `FAILED` on process stop.

---

### Why event + 60s reconciler, not only the event?

**Event:** `ContentDeployedEvent` is a Spring `ApplicationEvent` published
when a live publication record is created. Same pattern as batch
`RuntimeInstanceCreatedEvent` and Dialob process events. Javadoc: listeners
**must not throw** (an exception would abort the publisher’s flow).

**Why the event is not enough:**

1. It fires when the publication is **already live at create time**. A
   publication scheduled for next Monday must **not** index on Friday.
2. A listener can miss (restart, other replica, in-flight skip).
3. limaone’s cache can still show the previous HEAD for a few seconds.

**Reconciler:** `@Scheduled` every
`eveli.metis.search.live-publication-check-seconds` (default 60). Calls the
same `MetisLiveIndexTrigger.startIfLivePublicationChanged()`. That is the
go-live path for `liveDate` in the future, and the catch-up path.

**In-flight skip is intentional:** If a job is already running, the trigger
logs and returns. The next 60s tick retries. No immediate nested start.

**`publication_id` + `bundle_hash`:** `isPublicationIndexed` counts jobs in
`RUNNING|CANCELLING|COMPLETED` for that publication id **and** matching
bundle hash (the limaone world hash). A cacheless read that still saw the
previous HEAD cannot permanently skip the live publication: the hash would
not match, so the next tick starts another job.

**One entry point:** Boot auto-reindex, manual reindex, and deploy all go
through `MetisLiveIndexTrigger`, so the live publication id is stamped on
the job. `LivePublications` is the shared “what is live” helper.

---

### Why index with a dummy anonymous participant? Are auth-only services leaking?

**Article program:** limaone compiles the site for a **participant** (who is
asking). The portal, for an anonymous citizen, already runs it that way and
ships JSON that still **lists** auth-gated services; the UI then hides the
“open form” button via `isFormLinkEnabled`.

**Indexer:** Uses a dummy anonymous user (`metis-indexer`). Same visibility
as that public JSON. Search can find a login-required service; opening the
form is still gated.

**If the owner wants them out of search:** That is a content-model change.
The program would have to hide those topics from the site payload too.
Otherwise search and the site tree would disagree. This is written in
IMPLEMENTATION.md because it is a product choice, not an accident.

---

### Why is portal search public under `/portal/site/search`?

**Existing rule:** `SpringSecurityPolicy` already allows anyone on
`/portal/site/**` (the public site JSON). Search is a sub-resource of that
site, not a Metis-branded worker API. The portal asks for “site search”; it
should not know the implementation is Metis. If we later swapped the engine,
the path would stay.

**Not** `/worker/rest/api/metis/search`: that would require a citizen JWT and
would leak the platform name into the portal.

**Mitigations, because it is public:**

- In-memory rate limit keyed on `request.getRemoteAddr()`, **not**
  `X-Forwarded-For` (that header is spoofable). Behind a reverse proxy every
  citizen may share the proxy’s address — accepted: we fail closed to
  `fallback: true` for that bucket rather than trust a client-supplied IP.
- Logs store a **hash** of the query string, not the raw text.
- 5 s server timeout; bounded query queue; any failure → `fallback: true`.
- Hits are ids only. Mapping to labels happens in the browser against the
  site the citizen already loaded.

---

### Why `fallback: true` instead of HTTP errors?

**Product:** A citizen never sees “search unavailable.” The portal already
had client-side keyword search. Backend search **replaces** forms/topics when
it works; otherwise keyword stays.

**When the API returns `fallback: true` (HTTP 200):**

- Latest job is missing (empty table) or not `COMPLETED` (`RUNNING`,
  `CANCELLING`, `FAILED`, `CANCELLED`)
- Rate limit, timeout, full query queue, unexpected exception
- Status check itself throws

**When the API 404s:** Capability off (controller bean missing). The portal
treats 404/401/403 as “unavailable” and stops calling (does not retry every
keystroke).

**Frontend contract (`useBackendSearch` + `useSemanticResults`):**

- **pending:** form/topic lists rendered empty with `aria-busy`, so keyword
  results do not flash then get replaced
- **unavailable / fallback:** keyword search
- **ready with a real empty list:** “no results”, not a fallback
- Phones and hyperlinks always stay client-side

Empty job table is part of this contract: first boot with
`auto-reindex-on-startup` serves traffic immediately on keyword until the
first `COMPLETED`.

---

### Why hybrid RRF with those weights, and why the 0.45 floor?

**RRF:** Each hit gets `weight / (k + rank)`. Vector list uses
`vector-weight` 0.8; FTS list uses `fts-weight` 0.2. A document in both
lists gets both contributions. Rank matters more than raw similarity scores,
which are not on the same scale.

**Where the numbers came from:** One live municipal Finnish site. The table
in IMPLEMENTATION.md is a live retest (11 Sep 2026, a few hundred documents),
**not CI**. Query sets are kept locally and are not in this repository. Hybrid
precision@1 on the saved Finnish set is 20/20; two known misses on an older
set still fail. Do not retune from the armchair.

**Two empty-primary cases must not be mixed** (this used to be the easy bug):

1. **Embed failed** (timeout, Ollama busy, `Semaphore` reject). Hybrid
   returns **keyword ranking**, including the trigram fallback if primary FTS
   was empty. The citizen still gets something.
2. **Embed succeeded**, best vector score is below **0.45**, and FTS found
   nothing. Return **empty**. Gibberish (`qwerty`) lives in the 0.30–0.45
   band. Showing random services is worse than showing nothing.

**Trigram vs vector:** If primary FTS is empty but vectors survived 0.45,
trigram-only hits are **not** unioned into the vector list. Trigram on a
Finnish NL query pulls weakly similar words; the vector path already decided
this is a semantic query.

**Do not lower 0.45** to recover a weak NL query whose metadata lacks the
user’s words. Bump `metadata-prompt-version` and reindex so the LLM writes
that language into band C.

---

### Why a bounded query-embed semaphore instead of unlimited parallel embeds?

**What it serializes.** `EmbeddingService.embedQuery` takes a permit from
`query.embed-concurrency` (default: same as `query.concurrency`, **4**).
Index-time `embed()` does not. A Caffeine cache of the last 1 000 **finished**
query vectors is checked first. Identical **in-flight** strings join the same
`CompletableFuture` and do not take a second permit.

**Production (default 4).** Four overlapping unique searches all call Ollama.
`bge-m3` still runs **one** forward at a time on a single Ollama process
(Ollama forces `num_parallel=1` for embedding-only models); extra HTTP calls
queue there. On GPU that queue is tens to hundreds of milliseconds × 4, inside
the ~3 s embed timeout, so all four keep semantic ranking. That is the point:
do not fail-fast to FTS while Ollama would still have answered in time.

**Local CPU** sets `eveli.metis.search.query.embed-concurrency: 1` in
`application-dev.yml`. A second *different* uncached query fails `tryAcquire`
and hybrid continues as **server keyword ranking** (`fallback: false`). Same
string still coalesces.

**Two citizens, production defaults:**

| | User A “kirjasto” | User B “hammashoito” |
| --- | --- | --- |
| Permits | 1 of 4 | 2 of 4 |
| Hybrid | RRF | RRF |
| HTTP | results, no fallback | results, no fallback |

**Two citizens, local `embed-concurrency: 1`, different strings:** B is FTS-only
for that request. Same string: B joins A.

**Why not unlimited permits?** Dozens of unique cold queries would sit in
Ollama’s FIFO until the 5 s search budget dies → `fallback: true` for the
tail, worse than FTS-only. Fail-fast past the cap is overload protection.

**Why not wait on a *different* query’s embed?** That burns B’s timeout on A’s
vector, which B cannot use. Coalesce is same-string only.

Do not confuse this with the **per-IP rate limit** or a **full query thread
queue** (both `fallback: true`). Overflow of the embed cap is FTS-only, not
portal keyword fallback.

---

### Why `VECTOR(1024)` in SQL, not a property?

pgvector stores a fixed-width column. `bge-m3` is 1024 dimensions. That width
is in `V4_1`. A YAML property cannot ALTER a column.

The property `indexing.embedding-dimension` **asserts** at reindex start that
the live column still matches. Wrong-width writes would corrupt the column
or fail opaquely.

Changing model width (e.g. a 768-d model) is a **new migration**, not a
config edit. That is the point.

---

### Why not interrupt in-flight LLM calls on cancel?

**Cancel** sets the job to `CANCELLING` (still occupies the unique index) and
stops **submitting** further documents. Each document checks `isRunning`
before upsert. In-flight work is at most `indexing.concurrency` (default 1)
calls already inside Spring AI / Ollama. Those clients are not cooperatively
cancellable without a deeper HTTP abort story.

The README’s “the rest stop immediately” means **not-yet-started** documents.
That is true. It does not mean “Ollama drops the HTTP request this second.”

**Partial failure** (`markFailed` after some documents error) does **not**
call `deleteStale`. Old rows for removed services stay. The table is mixed;
the portal stays on `fallback: true` until an operator runs a job that
`COMPLETED`. Tested in the integration suite. Mixing “delete stale on
failure” would drop services the job never got to refresh.

---

### Why is the worker UI a new page instead of hanging off publications?

Reindex is an **operational job**: progress, skip counts, cancel, replace,
embedding model id, last error. Publications already have create / liveDate /
delete. Sticking a progress bar on a publication card would mix two
lifecycles and would not exist when the operator wants to rebuild after a
prompt-version bump with no new publication.

The Metis page is the capability status surface the README tells the next
capability to copy (`GET …/metis/status` plus a capability-specific panel).

**Who can do what.** Digiexpress has **two** worker IAM catalogs. They are not
the same strings, and Metis uses both the way publications already do.

| Catalog | Where it lives | What it is |
| --- | --- | --- |
| JWT / Spring roles | `eveliPermissions.yaml`, enforced by `SpringSecurityPolicy` | `ROLE_Authorized`, `ROLE_ASSET_ADMIN`, `ROLE_TASK_WORKER`, … Prefix `ROLE_` is JWT; `mapIamRole` strips it in the UI. |
| UI permissions | `user.permissions` from userinfo / the permissions service | `RELEASE_VIEW`, `STENCIL_EDIT`, `BATCH_VIEW`, … Typed in `IamApi.UserPermission`. Sidebar and buttons only; **not** HTTP. |
| Tenant feature | `eveli.tenant-features` contains `metis` | Environment chrome. Hides the sidebar item even if the user has `RELEASE_VIEW`. Does not start beans and does not grant API access. |

There is no `METIS_VIEW` / `ROLE_METIS_ADMIN`. Adding those would be a new
product type every tenant must assign. We reused the two roles that already
mean “this person works with published content.”

| Action | Who | Why that existing role |
| --- | --- | --- |
| See the sidebar item | Tenant feature `metis` **and** `NAV_TO_METIS` = `RELEASE_VIEW` or `RELEASE_EDIT` | Same mapping as `NAV_TO_RELEASES` (publications). The index is the live publication; the people who already look at releases are the audience. |
| `GET /metis/status`, `GET /metis/search/status` | `ROLE_Authorized` | Same as IAM, version, tenant-config, batches: any logged-in worker. The UI still only *navigates* there if they have `NAV_TO_METIS`. Knowing the URL is enough to read status, like batches. |
| `POST …/reindex` and `…/cancel` | `ROLE_ASSET_ADMIN` | Same role as **all** `/worker/rest/api/assets/**` (including publications). Those are the people who can change the bundle the indexer reads. A task worker must not start a multi-hour LLM job. Buttons use `canManageIndex` = JWT role `ASSET_ADMIN` after `mapIamRole`. |

**Are they appropriate?** Yes, as reuse of the publication/asset split:

- **Read status** is cheap and operational. `ROLE_Authorized` matches batches
  and tenant-config, not a new admin role.
- **Mutate the index** is as privileged as publishing. `ROLE_ASSET_ADMIN` is
  the role that can already POST a publication and thus change what search
  will index. Tightening it further would invent IAM nobody has been given.
- **Nav on `RELEASE_*`** is the UI half of that story. A `RELEASE_VIEW` user
  who is not `ASSET_ADMIN` sees the page and progress, not Rebuild. An
  `ASSET_ADMIN` without `RELEASE_VIEW` does not get the sidebar item but can
  still POST reindex if they have the URL — the same gap publications have
  (`NAV_TO_RELEASES` vs `/assets/**` requiring `ASSET_ADMIN`).

Do not volunteer a dedicated `METIS_*` permission. If the owner wants search
ops split from asset admins, that is a tenant IAM product change, not a
Metis cleanup.

Confirm dialogs no-op while `busy` so a double-click does not fire two POSTs.

**409 Conflict — the command was valid, the claim was not free.**

`tryStart` is `INSERT … WHERE NOT EXISTS` an in-flight row. If another job is
`RUNNING` or `CANCELLING`, that insert returns no id. The client still
returns a `MetisSearchIndexStatus`: `accepted: false` plus the **current**
job (progress, state, error). Cancel does the same when there is nothing
in-flight to stop. `replace=true` that successfully marks the running job
`CANCELLING` and asks for a restart is `accepted: true`.

The controller maps that flag to HTTP so the *command* outcome is visible
without reading JSON:

| `accepted` | HTTP | Meaning |
| --- | --- | --- |
| `true` | **202 Accepted** | We took the work (job started, or cancel/replace was applied). |
| `false` | **409 Conflict** | State conflict: already a job, or cancel with nothing running. Body is still the status document. |

409 is RFC 9110 “request conflicts with the current state of the resource.”
It is not a bug, not 429 (rate limit), not 500. 200 with `accepted: false`
would look like success in logs and in `response.ok`. 202 vs 409 is the
same idea as “we queued it” vs “we did not.”

**Why `__root.ts` must pass 409 through.** The shared `uberFetch` treats
most non-OK statuses as thrown `Error`s (toast / login). 404 was already
an exception so “capability off” can be a quiet `undefined`. 409 is the
same class of *expected* business status: the Metis POST hooks parse the
JSON and the page shows `eveli.metis.search.conflict` plus the live job.
Without that pass-through, a double-click Rebuild would look like a
transport failure and lose the status body. Other 4xx/5xx still throw.

Progress shown in the UI is `(processedCount + skippedCount) / totalCount`.
Skipped (hash match) is real work that finished; hiding it makes a 90%
unchanged site look stuck at 10%.

---

### Why no TypeScript tests?

Worker fetch hooks and gamut search have historically been untested in TS in
this repo. The heavy coverage is Java: portal fallback, empty job table,
hybrid fusion, publication/bundle hash, permissions.

A Jest file for `backend-results.ts` (id mapping, all-unmapped → fallback,
`searchOnly` preference) would be a good follow-up. It is not a blocker for
this review and we are not volunteering a TS test suite for the worker page.

---

### Why is `spring-ai-starter-model-ollama` on every `eveli-app` / `eveli-app-gcloud` classpath?

**Release topology:** One artifact per app. We do not ship a second GCloud
image “with AI.” The Ollama starter is on the classpath; YAML sets
`spring.ai.model.chat/embedding: none` so the starter does **not** connect.
GCloud stays off until env / Secret Manager turns a given environment on.
`wrench-only` (authoring-only process) does not `@Import` Metis.

**Fair complaint:** Image size and CVE surface of a starter you are not
using. The alternative is a Maven profile or a second artifact. That is a
release-topology decision, not a smell inside `metis-client`.

---

### Why reflection to read the model id?

`/metis/status` shows `chatModel` / `embeddingModel` so an operator can see
what the process actually resolved. Ollama and OpenAI option types do not
share an interface. `EveliAutoConfigMetis` tries `getDefaultOptions().getModel()`
via reflection, then env keys (`spring.ai.ollama.*.model`, OpenAI equivalents).

If reflection fails, env is enough; search does not depend on the status
string. If the owner dislikes reflection, drop it and use env only.

---

### `deleteStale` builds `NOT IN (($1,$2),…)` — will that explode?

After a **successful** reindex, rows whose `(workflow_id, locale)` are not in
the current document list are deleted (a service that left the site should
leave the index). The SQL is:

```sql
DELETE FROM metis_search_index
WHERE (workflow_id, locale) NOT IN (($1,$2),($3,$4),…)
```

PostgreSQL allows 65535 bind parameters. Two parameters per key ⇒ tens of
thousands of rows. Current sites are hundreds of documents × a few locales.

Rewrite to a temp table / `UNNEST` when a customer is actually that large,
not now.

---

### pgvector `ORDER BY embedding <=> $1 LIMIT` plus `locale = $2`

**HNSW** is an approximate nearest-neighbour index. A `WHERE locale = 'fi'`
can be applied **before** or **after** walking the graph. If the planner
walks the global index then filters, a small locale could get fewer
neighbours than you asked for (classic pre-filter vs post-filter issue).

At a few hundred rows per locale this does not show up. A **partial HNSW**
`WHERE locale = 'fi'` (one index per locale) is a later migration if a
tenant is huge. Not now.

---

## Do not volunteer

These are known limits or future work. Offering them in the review invites a
rewrite we are not staffed for.

| Topic | Why not now |
| --- | --- |
| Multi-tenant / `cockpit_id` | No customer. Speculative schema. |
| Changing embedding dimension without a migration | Would corrupt `VECTOR(1024)`. |
| Interrupting Ollama mid-request on cancel | Needs a cancellable HTTP client; in-flight is at most concurrency (default 1). |
| Queuing different queries behind each other’s embed | Burns the 5 s budget on a vector the waiter cannot use; fail-fast to FTS past `embed-concurrency`. |
| True parallel `bge-m3` on one Ollama | Ollama forces embedding models to `num_parallel=1`; extra HTTP calls queue. |
| Partial HNSW indexes per locale | Hundreds of rows; not the bottleneck. |
| Rewriting `deleteStale` | Parameter limit is far above current sites. |
| Moving Flyway into `metis-client` | Host owns migrations everywhere else. |
| Making `metis-client` provider-agnostic beyond `spi.ai` | Wrappers exist; constructors mentioning Spring AI is the same as Thena `Pool`. |
| TypeScript test suite for the worker page | Follow-up; Java covers the contracts. |
| Feedback analysis | Proposal only, `V5_x`. Out of this review. |

---

## File map

| Area | Where | What to look at |
| --- | --- | --- |
| Library API, ranking, index | `mvn_setup/metis-parent/metis-client` | Product rules, SQL, RRF, job protocol |
| Platform vs capability flags | `EveliAutoConfigMetis`, `EveliAutoConfigMetisSearch`, `EveliAutoConfigMetisFlyway` | Same `@Import` style as batches/gamut |
| Flyway (runtime schema) | `eveli-client/.../db/metis/search/V4_1__metis_search.sql` | Source of truth, including comments |
| Reindex triggers | `spi/metis/search/*`, `LivePublications`, `ContentDeployedEvent` | Event + 60s reconciler + boot |
| Portal HTTP | `GamutSiteSearchController` | Public, rate limit, `fallback: true` |
| Worker HTTP | `MetisApiController`, `MetisSearchApiController` | Status vs mutate ACL |
| Worker UI | `eveli-metis/`, `secured.$locale.worker.metis*` | Status, progress, asset-admin buttons |
| Portal UI | `useBackendSearch`, `backend-results`, `GPopoverSearch`, `SearchResults` | Debounce, abort, mapping, fallback |
| Ops | `mvn_setup/metis-parent/README.md` (Site search), `docs/README_CONFIG_PROPERTIES.md` | Flags, first deploy, GCloud, `*:missing` |
| Permissions | `eveliPermissions.yaml`, `PropertyAuthorizationTest` | No blanket `/metis/**` |

Generated and should not be hand-argued: `fetchTree.gen.ts`,
`routeTree.gen.ts`.
