# Metis

Metis is the AI platform for digiexpress. It owns the model configuration and the shared
primitives, and each thing built on top of it is a **capability** with its own client, its own
configuration sub-tree, its own migrations and its own endpoints.

Semantic site search is the first capability.

| Capability | Id | Status | Docs |
| --- | --- | --- | --- |
| Semantic site search | `site-search` | Implemented | [How it works](docs/site-search/IMPLEMENTATION.md) · [this README](#semantic-site-search) |
| Feedback analysis | `feedback` | Proposal only | [docs/feedback-analysis/PROPOSAL.md](docs/feedback-analysis/PROPOSAL.md) |

The library is `metis-client` (`io.resys.metis`). Configuration, REST and Flyway live in
`eveli-client`, the same split as `eveli-permissions`. Properties are listed in
[docs/README_CONFIG_PROPERTIES.md](../../docs/README_CONFIG_PROPERTIES.md).

> [!IMPORTANT]
> **How enable works today — two client blockers, neither is Flyway.**
>
> **1. pgvector is not in `V4_1`.** Flyway only creates built-in-type tables so vanilla
> Postgres still migrates with the flags off. The `vector` / `pg_trgm` / `unaccent`
> extensions, `embedding VECTOR(1024)` column, and HNSW / trigram indexes are applied
> at boot by `MetisSearchSql.ensureSearchExtensions` when `eveli.metis.search.enabled=true`.
> Search enable **fails boot** if that SQL cannot run (no pgvector binaries, or the app
> role cannot `CREATE EXTENSION`).
>
> **2. Ollama models are not in the app image and must be pulled.** First enable needs
> `bge-m3` and `llama3.2` already on the Ollama volume. `ollama pull` talks to the
> **public** registry (`registry.ollama.ai`) over outbound HTTPS. Air-gapped or
> egress-locked clients **cannot** use `ollama-init` / `ollama pull`. Plan this before
> turning the flags on: pre-seed the Ollama data directory from a machine that can pull,
> `ollama create` from a local GGUF, or an internal mirror. If the model is already on
> the volume, pull is skipped. Details in [First deployment](#first-deployment).

---

## Layout

```
io.resys.metis
├── api                  MetisClient, MetisConfig, MetisStatus
├── spi                  MetisClientImpl: capability wiring and platform status
├── spi.ai               EmbeddingService, StructuredChatService  (shared, capability-agnostic)
└── search               the semantic site search capability
    ├── api              MetisSearchClient, MetisSearchConfig, MetisSearchIndexStatus, ...
    └── spi              MetisSearchClientImpl, spi.index, spi.query
```

`spi.ai` never imports a capability. `MetisClient` and `MetisClientImpl` know capabilities by
name only to expose them (`search()`) and report their state.

`spi.ai` is where a primitive belongs once a second capability would need it:

- `EmbeddingService` — embeddings, with a shared query cache and a bounded fail-fast
  guard. In-flight query embeds are capped at `eveli.metis.search.query.embed-concurrency`
  (default: same as `query.concurrency`). A further unique query is rejected rather than
  queued; hybrid search then continues as keyword ranking. Identical in-flight strings
  join the same future. Index-time `embed()` is not gated by this semaphore.
- `StructuredChatService` — prompt the chat model for a typed answer, with a bounded retry and an
  empty result rather than an exception, so a caller can fall back. The prompt always stays with
  the capability

## Configuration

The platform and every capability have separate flags. `eveli.metis.enabled` wires the models and
`/metis/status` and nothing else; a capability additionally needs its own flag. Enabling the
platform alone is a valid deployment, it just has no capabilities.

The worker UI is a third switch. `metis` in `eveli.tenant-features` (or the same flag on a user
profile) shows the Metis sidebar item and the status/reindex page. It does not start the
platform; without `eveli.metis.enabled` the page still opens but reports that Metis is off.

| Key | Meaning |
| --- | --- |
| `eveli.metis.enabled` | Platform: model beans, shared primitives, `GET /metis/status` |
| `spring.ai.model.chat`, `spring.ai.model.embedding` | Provider, must not stay `none` |
| `spring.ai.ollama.*` | Provider URL and model ids |
| `eveli.metis.<capability>.enabled` | One capability |
| `eveli.metis.<capability>.*` | That capability's tuning |
| `eveli.tenant-features` including `metis` | Worker UI: sidebar and `/secured/$locale/worker/metis/` |

## Endpoints

`GET /worker/rest/api/metis/status` is the platform status: whether Metis is enabled, the resolved
provider and models, and one entry per capability with its own state.

```json
{
  "enabled": true,
  "provider": "ollama",
  "chatModel": "llama3.2",
  "embeddingModel": "bge-m3",
  "capabilities": [
    { "id": "site-search", "enabled": true, "state": "READY", "detail": "174 documents indexed" }
  ]
}
```

Capability state is one of `READY`, `NOT_READY`, `DISABLED` or `ERROR`, and is a summary. Anything
more detailed belongs on the capability's own status endpoint, for example
`GET /worker/rest/api/metis/search/status` for the reindex job.

Everything else lives under `/worker/rest/api/metis/<capability>/`.

---

## Semantic site search

Semantic search over published portal content. It indexes each workflow link per locale into
PostgreSQL (`tsvector` + embedding) and fuses vector and full-text rankings with reciprocal rank
fusion.

The code is `io.resys.metis.search` in `metis-client`. Configuration, REST and Flyway live in
`eveli-client`. How it works end to end is in
[docs/site-search/IMPLEMENTATION.md](docs/site-search/IMPLEMENTATION.md). Properties are listed in
[docs/README_CONFIG_PROPERTIES.md](../../docs/README_CONFIG_PROPERTIES.md).

This is wired into **eveli-app** and **eveli-app-gcloud**, off by default. It needs two flags:
`eveli.metis.enabled` for the platform and `eveli.metis.search.enabled` for this capability, plus
the `spring.ai.model.*` keys (env, Secret Manager, or YAML).

### First deployment

Operator runbook for `eveli-app` / `eveli-app-gcloud` on any client. You can roll this
release **without** enabling Metis. Enabling search later is a separate, explicit step.

#### This release vs enabling search

See the **IMPORTANT** callout at the top of this README. Short version: Flyway always
applies `db/postgresql/V4_1__metis_search.sql` (built-in types only). Extra unused tables
are expected. Vanilla `postgres:17` is enough while the flags stay off. pgvector and
Ollama are required only when you turn search **on**; the ensure is
`MetisSearchSql.ensureSearchExtensions`, not Flyway. If it fails, search enable fails boot.

Local DBs that already applied an older `V4_1` from `db/metis/search` will see a Flyway checksum
mismatch. Run `flyway repair` on those databases. Fresh databases just apply the new `V4_1`.

#### 1. Database

**All clients (flags off).** Official PostgreSQL of the same major as the rest of the stack, with
contrib available for a later enable. Cloud SQL / RDS: no image change for tables-only.

**When enabling search:**

- Self-hosted: the server must have pgvector binaries. Local compose uses `pgvector/pgvector:pg17`
  (official PostgreSQL 17 plus pgvector). Switching `postgres:17` → `pgvector/pgvector:pg17` on
  the **same volume / `PGDATA`** does not rewrite existing databases, tables, or rows. No
  dump/restore. **Do not delete or recreate the volume** — that would wipe data. Do not jump
  majors this way (`17` → `18` needs `pg_upgrade` or dump/restore). After the image switch,
  pgvector is *available*, not *enabled*; the column and indexes appear on the next boot with
  search enabled.
- Cloud SQL / RDS: there is no image switch. Allow-list `vector`, `pg_trgm`, `unaccent`. Existing
  data is untouched. The app role often cannot `CREATE EXTENSION`; a DBA should pre-create all
  three before setting the flags:
  `CREATE EXTENSION IF NOT EXISTS vector; CREATE EXTENSION IF NOT EXISTS pg_trgm; CREATE EXTENSION IF NOT EXISTS unaccent;`
- Column width is `VECTOR(1024)` (matches `bge-m3`). A different embedding width is a schema
  change, not a property change.

#### 2. Models / network

Semantic site search needs a reachable chat model (metadata during index) and embedding model
(index + search). `eveli-app` and `eveli-app-gcloud` both ship the Ollama starter.

- Ollama (or compatible) must be reachable from the **app** process, typically `http://<host>:11434`.
- `SPRING_AI_OLLAMA_BASE_URL` must not stay `localhost` in GKE / Cloud Run; that talks to the
  container itself.
- Models must exist **before the first reindex**, not only before boot: `bge-m3` (1024-d
  embeddings) and `llama3.2` (metadata).
- `ollama pull` downloads from Ollama’s public registry (`registry.ollama.ai`) and needs outbound
  HTTPS. Clients that prohibit egress cannot rely on `ollama-init`. Offline options: copy a
  pre-populated Ollama data directory, `ollama create` from a local GGUF, or an internal mirror.
  If the model is already on the volume, `ollama-init` skips pull.
- CPU-only Ollama: set `eveli.metis.search.query.embed-concurrency=1` and leave
  `indexing.concurrency` at `1`. A few hundred documents take hours. Do not gate liveness on
  reindex completion.

#### 3. Application config

Required on the **eveli-app** or **eveli-app-gcloud** process (env vars, Secret Manager, or YAML —
same keys) **when enabling search**. Both flags and the two `spring.ai.model.*` values must be set
together: the platform flag alone gives model beans and `/metis/status` but no search; search
without the platform fails boot; the platform without a provider fails boot because Metis needs
`EmbeddingModel` and `ChatClient` beans.

The worker UI is a third switch. `metis` in `eveli.tenant-features` (or on a user profile) shows
the sidebar item. It does not start the platform.

| Key | First-enable value | Notes |
| --- | --- | --- |
| `eveli.metis.enabled` | `true` | Platform: models, shared primitives, `/metis/status` |
| `eveli.metis.search.enabled` | `true` | This capability. Triggers the pgvector ensure at boot |
| `spring.ai.model.chat` | `ollama` | Must not stay `none` |
| `spring.ai.model.embedding` | `ollama` | Must not stay `none` |
| `spring.ai.ollama.base-url` | provider URL | e.g. `http://localhost:11434` locally |
| `spring.ai.ollama.embedding.options.model` | `bge-m3` | Must match `VECTOR(1024)` |
| `spring.ai.ollama.chat.options.model` | `llama3.2` | Used only during indexing |

Recommended:

| Key | Suggested | Notes |
| --- | --- | --- |
| `eveli.metis.search.locales` | `en, fi, sv` | Only these three have a PostgreSQL stemmer |
| `eveli.metis.search.auto-reindex-on-startup` | `true` | Builds the first index when the table is empty |
| `eveli.metis.search.reindex-on-deployment` | `true` | Incremental reindex after a live publication |
| `eveli.metis.search.live-publication-check-seconds` | `60` | Picks up a scheduled publication once it is live |
| `eveli.metis.search.query.embed-concurrency` | unset (= `query.concurrency`, 4) | Overlapping citizen searches keep semantic ranking. Set `1` on CPU-only Ollama. |

`eveli-app` and `eveli-app-gcloud` both `@Import` `EveliAutoConfigMetisFlags` (always),
`EveliAutoConfigMetis` and `EveliAutoConfigMetisSearch` (each conditional on its own flag).

On GCloud, YAML stays off. Turn a given environment on with env or Secret Manager (the process
already imports `sm://`):

```
EVELI_METIS_ENABLED=true
EVELI_METIS_SEARCH_ENABLED=true
SPRING_AI_MODEL_CHAT=ollama
SPRING_AI_MODEL_EMBEDDING=ollama
SPRING_AI_OLLAMA_BASE_URL=http://<ollama-host>:11434
```

Model names can stay at the YAML defaults (`bge-m3`, `llama3.2`) unless you override them.

Worker JWT roles after enable:

- `GET /worker/rest/api/metis/search/status` — `ROLE_Authorized`
- `POST /worker/rest/api/metis/search/reindex` — `ROLE_ASSET_ADMIN`
- `POST /worker/rest/api/metis/search/reindex?replace=true` — stop the running job and start a new one
- `POST /worker/rest/api/metis/search/reindex/cancel` — stop the running job without starting another

In-flight work is at most `eveli.metis.search.indexing.concurrency` documents (default 1). The rest
stop immediately. Poll `GET /worker/rest/api/metis/search/status` until the state is no longer
`RUNNING` or `CANCELLING`.

#### 4. Kubernetes replicas

Several pods may try to start the same reindex at once (startup, deploy event, 60 s reconciler,
or a manual POST). A partial unique index allows at most one row in `RUNNING` or `CANCELLING`.
The winner inserts the job. Losers get `accepted: false` and the worker API returns **HTTP 409**.
That is the lock working, not a cluster failure. Loser pods stay healthy and keep serving the last
`COMPLETED` index (or keyword fallback).

If the winner dies, `last_progress_at` plus `eveli.metis.search.indexing.abandoned-after-seconds`
(default 3600) fails the stuck row so another pod can claim. A clean shutdown marks the local job
`FAILED` and releases the claim.

The portal per-IP rate limit is in-memory **per pod**. Do not gate liveness or readiness on reindex
completion; the job runs in the background.

#### 5. Content and first index

Semantic site search indexes the **live published** bundle. Authoring import does not trigger a reindex.
There must be a live publication, or the job fails without touching rows.

With `auto-reindex-on-startup: true` the first boot starts the job in the background. The portal
serves traffic immediately and falls back to keyword search (`fallback: true`) until a job
`COMPLETED`. Scheduled publications (`liveDate` in the future) are indexed when they go live (the
60 s reconciler), not at create time. Immediate publishes still start a job right away.

To force a rebuild later: `POST /worker/rest/api/metis/search/reindex?force=true`. Switching
`spring.ai.ollama.embedding.options.model` or bumping
`eveli.metis.search.indexing.metadata-prompt-version` already invalidates stored hashes, so a
normal reindex reprocesses those rows.

#### 6. Check

- App boots. With flags off, Flyway has applied `V4_1` and there is no pgvector requirement.
- After enable: `GET /worker/rest/api/metis/status` reports the platform, with `site-search` in the
  capability list
- `GET /worker/rest/api/metis/search/status` → `COMPLETED` (or `RUNNING` on first index). Treat
  `COMPLETED` as done only when `processedCount + skippedCount` equals `totalCount`.
- `GET /portal/site/search?q=...&locale=fi` returns results, or `fallback: true` while a reindex
  is in flight or the latest job is `FAILED` / `CANCELLED` (until the next `COMPLETED`)

#### What typically goes wrong

| Symptom | Cause | Action |
| --- | --- | --- |
| Flyway validate fails on `V4_1` checksum | This database already applied the old `db/metis/search` script | `flyway repair` |
| Boot fails with CREATE EXTENSION / vector | Search enabled on vanilla Postgres, or app role cannot create extensions | Switch to `pgvector/pgvector:pg17` **keeping the volume**, or DBA pre-creates extensions |
| Data gone after “switching to pgvector” | Volume / `PGDATA` was deleted | Restore from backup. Image switch must keep the volume |
| Search enable fails, Ollama connection refused | `SPRING_AI_OLLAMA_BASE_URL` still localhost inside the cluster | Point it at the Ollama service |
| Reindex fails, model not found | Empty Ollama volume and no pull (air-gap or init skipped) | Pre-seed models or allow `registry.ollama.ai` |
| Job `FAILED`, no rows | No live publication | Publish, then reindex |
| HTTP 409 / `accepted: false` from another pod | Replica lock | Ignore; one winner is enough |
| `COMPLETED` but portal still `fallback: true` | `processedCount + skippedCount` < `totalCount`, or a later job failed | Re-run reindex; treat incomplete COMPLETED as not done |

### Local development

```bash
cd mvn_setup/eveli-parent/eveli-local-docker && docker compose up -d postgresql ollama ollama-init
```

`ollama-init` pulls `bge-m3` and `llama3.2` from `registry.ollama.ai` unless they are already in
`./_db/ollama_data` (large download on first run). Air-gapped machines must pre-seed that volume.
`application-dev.yml` already enables both flags and sets the Ollama models. `spring-boot:run`
pins `spring.config.location` to the profile files only, so the base `application.yml` is not
read locally — keep the flags in `application-dev.yml`:

```yaml
eveli.metis.enabled: true
eveli.metis.search.enabled: true
eveli.metis.search.query.embed-concurrency: 1
spring.ai.model.chat: ollama
spring.ai.model.embedding: ollama
```

`embed-concurrency: 1` keeps CPU Ollama at one query embed at a time. Production leaves it unset so it matches `query.concurrency` (4).

To index a limaone dump without authoring, activate the `prod` profile (and typically
`eveli.assets.enabled=false` so the runtime uses the external provider) with an `EveliDeployment`
JSON at `eveli-app/src/main/resources/test-assets.json` (gitignored). `AppConfigProd` loads that
file as the classpath world. Then boot with `auto-reindex-on-startup`, or
`POST /worker/rest/api/metis/search/reindex`.

### Portal behaviour

`GET /portal/site/search` is public and deliberately not Metis-branded. Errors, a full search
queue, the per-IP rate limit, or a reindex that has not `COMPLETED` answer
`{"fallback": true, "results": []}`; the portal then uses client-side keyword search. The same
happens when the capability is off (the endpoint 404s). A slow embedding model, or more
in-flight unique query embeds than `query.embed-concurrency`, does **not** take that path:
hybrid returns keyword ranking and omits vector fusion.

Ranking, the document model, and the 0.45 empty-query floor are in
[IMPLEMENTATION.md](docs/site-search/IMPLEMENTATION.md).

---

## Adding a capability

Say the capability is `feedback`. Each step has a matching one in semantic site search to copy from.

1. **Package.** `io.resys.metis.feedback.api` for the client and its config, `…feedback.spi` for
   the implementation. Depend on `spi.ai` for models, never on another capability. If a primitive
   turns out to be generic, move it into `spi.ai` rather than reaching across.
2. **Config sub-tree.** `eveli.metis.feedback.enabled` plus `eveli.metis.feedback.*`, bound by a
   new `EveliPropsMetisFeedback`. Keep the metis-client config type separate from the Spring
   properties type, as `MetisSearchConfig` and `EveliPropsMetisSearch` are, so the library stays
   independent of the hosting application.
3. **Flyway band.** Add `V5_x` under `db/postgresql` like any other eveli-client migration
   (semantic site search is `V4_x`). Prefix tables with `metis_feedback_`. Do not add a separate
   Flyway location or gate migrations on a capability flag.
4. **Wiring.** A new `EveliAutoConfigMetisFeedback`, conditional on
   `eveli.metis.feedback.enabled`, and added to the `@Import` list of both `Application` classes.
   It owns its beans, controllers and any listeners.
5. **Endpoints.** `/worker/rest/api/metis/feedback/…`, with `…/status` reporting the capability's
   own state. Do not add anything to `MetisApiController`; instead surface a one-line summary
   through the capability list in `MetisClientImpl`.
6. **Permissions.** Explicit entries in `eveliPermissions.yaml` (and the test copy). There is
   deliberately no blanket `/worker/rest/api/metis/**` rule, so a new capability is unreachable
   until it is granted a role. `PropertyAuthorizationTest` asserts that.
7. **Docs.** `docs/feedback/README.md`, a row in the capability table above, and a section in
   [docs/README_CONFIG_PROPERTIES.md](../../docs/README_CONFIG_PROPERTIES.md).

---

## Build

```bash
cd mvn_setup/metis-parent && mvn install
```

The Testcontainers test in `eveli-client` starts `pgvector/pgvector:pg17` and stubs the models.
No Ollama required for tests.
