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
| `spring.ai.model.chat`, `spring.ai.model.embedding` | Provider, `ollama` or `google-genai`, each set independently. Must not stay `none` |
| `spring.ai.ollama.*` | Ollama URL and model ids |
| `eveli.metis.google-genai.*` | Gemini on Vertex AI: project, location, model ids |
| `eveli.metis.<capability>.enabled` | One capability |
| `eveli.metis.<capability>.*` | That capability's tuning |
| `eveli.tenant-features` including `metis` | Worker UI: sidebar and `/secured/$locale/worker/metis/` |

## Google GenAI (Vertex AI)

Gemini on Vertex AI is the second provider next to Ollama. Spring AI's value for it is
`google-genai`, set on `spring.ai.model.chat` and/or `spring.ai.model.embedding`; the two are
independent, so chat on Vertex with `bge-m3` on Ollama also works.

### How it is wired

- Only the plain Spring AI modules `spring-ai-google-genai` and `spring-ai-google-genai-embedding`
  are on the classpath (both apps; optional in `eveli-client`), not the Google starters. The
  starters' embedding connection bean is unconditional and they select on a second key
  (`spring.ai.model.embedding.text`), which would break every deployment that does not use Vertex.
- `EveliAutoConfigMetisGoogleGenAi` (imported by both apps) creates the Google `Client`, the
  `ChatModel` and the `EmbeddingModel`, each only when its key is `google-genai` and
  `eveli.metis.enabled` is true. Ollama's auto-configuration turns itself off for any value other
  than `ollama`. Nothing Google-related is created otherwise.
- `build-parent` pins `google.genai.version` (1.73.0). Spring AI 1.1.8 brings 1.37.0, which builds
  a wrong host for the `eu` multi-region and sends embeddings to `:predict`, which
  `gemini-embedding-2` rejects. Keep the pin until Spring AI brings 1.73.0 or later itself.
- Authentication is Application Default Credentials. There is no Metis property for it.
- Spring AI 1.1.x never sends an embedding `taskType`, so documents and queries are embedded the
  same way.

### What to set

| Key | Value | Notes |
| --- | --- | --- |
| `spring.ai.model.chat`, `spring.ai.model.embedding` | `google-genai` | Either or both |
| `eveli.metis.google-genai.project-id` | GCP project | Required once selected; billed for Vertex |
| `eveli.metis.google-genai.location` | `eu` | Required once selected. One location for both models, see below |
| `eveli.metis.google-genai.chat.model` | `gemini-3.1-flash-lite` (default) | Also `.temperature` (`0.2`) and `.thinking-level` (`MINIMAL`; thinking tokens are billed as output) |
| `eveli.metis.google-genai.embedding.model` | `gemini-embedding-2` (default) | |
| `eveli.metis.google-genai.embedding.dimensions` | `1024` (default) | Must equal `eveli.metis.search.indexing.embedding-dimension`; the reindex fails fast otherwise |
| `GOOGLE_APPLICATION_CREDENTIALS` (env) | path to a service-account key | Only where there is no Workload Identity, e.g. locally |

Suggested with Vertex: `eveli.metis.search.indexing.concurrency: 4`,
`eveli.metis.search.indexing.document-timeout-seconds: 120` and `spring.ai.retry.max-attempts: 3`
(`StructuredChatService` already retries).

Environment-variable form:

```
EVELI_METIS_ENABLED=true
EVELI_METIS_SEARCH_ENABLED=true
SPRING_AI_MODEL_CHAT=google-genai
SPRING_AI_MODEL_EMBEDDING=google-genai
EVELI_METIS_GOOGLEGENAI_PROJECTID=<project>
EVELI_METIS_GOOGLEGENAI_LOCATION=eu
```

### Location and models

`eu` is the EU multi-region endpoint (`aiplatform.eu.rep.googleapis.com`). It keeps processing in
the EU and is the only EU location that serves both default models. Checked on 2026-09-25:

| Model | `eu` | `global` | single EU regions (`europe-west1/4`, `europe-north1`) |
| --- | --- | --- | --- |
| `gemini-3.1-flash-lite` | yes | yes | no |
| `gemini-embedding-2` | yes | – | – |
| `gemini-embedding-001` | no | yes | yes |

`global` also works for chat but may process requests outside the EU. Recheck before production;
Google adds locations over time.

### GCP setup

Replace `PROJECT`, and `NS` / `KSA` with the namespace and Kubernetes service account the
`frontdesk-app` pod runs as.

```bash
gcloud services enable aiplatform.googleapis.com --project PROJECT
gcloud iam service-accounts create metis-vertex --project PROJECT --display-name "Metis Vertex AI caller"
gcloud projects add-iam-policy-binding PROJECT \
  --member "serviceAccount:metis-vertex@PROJECT.iam.gserviceaccount.com" --role roles/aiplatform.user
```

On GKE, bind it through Workload Identity; no key file, no secret, no env var:

```bash
gcloud iam service-accounts add-iam-policy-binding metis-vertex@PROJECT.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser --member "serviceAccount:PROJECT.svc.id.goog[NS/KSA]"
kubectl annotate serviceaccount KSA -n NS \
  iam.gke.io/gcp-service-account=metis-vertex@PROJECT.iam.gserviceaccount.com
```

Locally, either `gcloud auth application-default login` (runs as you), or create a JSON key for
`metis-vertex` (console: Service Accounts → Keys → Add key), keep it outside the repository and
start with `GOOGLE_APPLICATION_CREDENTIALS` pointing at it (runs with the deployed permissions).

Also: check the Vertex quotas on `eu` for both models, and set a budget alert on the project.

### Before production

- **Data leaves the cluster:** site content (public), citizen search strings and, once feedback
  analysis is wired, feedback text. Google's Vertex terms exclude training on customer data. This
  needs sign-off under the customer's data-processing agreement. If search strings may not leave,
  keep `spring.ai.model.embedding: ollama` and only chat on Vertex.
- **Use the Gemini score thresholds** below, not the Java defaults, which are the `bge-m3`
  calibration. Recheck them after a large content change or a metadata prompt change.
- **Switching the embedding model reindexes by itself.** The portal serves keyword search until
  the job with the new model completes (see [Content and first index](#5-content-and-first-index)).

### Score thresholds

Cosine scores depend on the embedding model, so the three query thresholds are per model. The Java
defaults are the `bge-m3` calibration; `application-dev.yml` and the gcloud `application.yml`
set the Gemini values.

| Property | `bge-m3` (Java default) | `gemini-embedding-2` |
| --- | --- | --- |
| `eveli.metis.search.query.min-vector-score` | 0.30 | 0.60 |
| `eveli.metis.search.query.min-vector-score-without-keyword` | 0.45 | 0.63 |
| `eveli.metis.search.query.score-drop-off-ratio` | 0.90 | 0.95 |

Calibrated on 2026-09-25 against the local 258-document index (120 fi, 115 sv, 23 en) with a
local query set, `docs/site-search/eval-queries.json` (gitignored): 38 real queries and 19 noise
queries (gibberish, pizza, football, bitcoin and car repairs per locale). Scores were taken from
the real `HybridSearchService` over a grid of 246 combinations.

- Gemini scores run higher and closer together than `bge-m3`. Noise scores 0.47–0.62 (max
  0.616); the weakest real query scores 0.652. With the `bge-m3` values every noise query
  returned results.
- `min-vector-score-without-keyword` decides most queries: 25 of the 38 real queries and all
  noise have no primary keyword hit. 0.63 sits in the gap; 0.62–0.65 gave identical results, 0.70
  empties two real queries. The margin is about 0.02 on each side.
- `min-vector-score` does not change this set. It drops the vector list when a nonsense query
  happens to hit a keyword; real queries with a keyword hit all score 0.685 or more.
- `score-drop-off-ratio` sets how many related services show. 0.95: 38/38 first, 1.8 results per
  query. 0.92: 37/38 first, 2.7 results. 0.90: 37/38, 3.4 results. A neighbour boosted by a
  keyword hit took first place for "haluan vuokrata kunnan asunnon" below 0.95.

| Thresholds | First place (of 38) | Noise with results (of 19) | Results per query |
| --- | --- | --- | --- |
| `bge-m3` values: 0.30 / 0.45 / 0.90 | 37 | 19 | 3.4 |
| Gemini values: 0.60 / 0.63 / 0.95 | 38 | 0 | 1.8 |

To recalibrate, embed each query in the eval file with the deployed model, rank it against the
index with `FtsSearchService` / `VectorSearchService` / `HybridSearchService` at zero thresholds,
and pick the no-keyword floor between the highest noise score and the lowest real best score.

### Cost

List prices checked 2026-09-25, 1 USD ≈ 0.87 EUR, VAT excluded. Vertex prices:
`gemini-3.1-flash-lite` $0.25 / $1.50 per 1M input / output tokens on `global`, taken here as
+10% for `eu` like regional endpoints (not separately published, verify);
`gemini-embedding-2` $0.20 per 1M text tokens. Example site: 900 documents (300 workflows × 3
locales), about 450 + 350 chat tokens and 600 embedded tokens each.

| | Ollama on GPU | Ollama on CPU | Vertex AI (`eu`) |
| --- | --- | --- | --- |
| Fixed cost per month | ≈ €410–550 per always-on node (spot ≈ €200), ×2 for HA | ≈ €80–120 of node capacity | €0 |
| One full reindex, 900 documents | included | included (takes hours) | ≈ €0.65 |
| Publish with 10% changed | included | included | ≈ €0.06 |
| 50,000 unique searches | included | included, often misses the 2 s query budget | ≈ €0.07 |
| 5,000 feedbacks classified and embedded | included | included | ≈ €3 |
| Typical month | ≈ €410–550 | ≈ €80–120 | **under €5** |

A fractional GPU node costs as much as about 800 full reindexes a month on Vertex. Self-hosting
pays off only when data residency or contract terms rule out a managed model.

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
Both also ship Gemini on Vertex AI (`google-genai`); see
[Google GenAI (Vertex AI)](#google-genai-vertex-ai) for its settings, GCP setup and cost. With
Vertex, `gemini-embedding-2` is asked for `1024` dimensions, so the `VECTOR(1024)` column stays.

Ollama:

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
| `spring.ai.model.chat` | `ollama` or `google-genai` | Must not stay `none` |
| `spring.ai.model.embedding` | `ollama` or `google-genai` | Must not stay `none`. Can differ from chat |
| `spring.ai.ollama.base-url` | provider URL | Ollama only. e.g. `http://localhost:11434` locally |
| `spring.ai.ollama.embedding.options.model` | `bge-m3` | Ollama only. Must match `VECTOR(1024)` |
| `spring.ai.ollama.chat.options.model` | `llama3.2` | Ollama only. Used only during indexing |
| `eveli.metis.google-genai.project-id` | GCP project | Google only. Billed for Vertex AI |
| `eveli.metis.google-genai.location` | `eu` | Google only. EU multi-region, one location for both models |
| `eveli.metis.google-genai.chat.model` | `gemini-3.1-flash-lite` | Google only. Also `.temperature` (`0.2`), `.thinking-level` (`MINIMAL`) |
| `eveli.metis.google-genai.embedding.model` | `gemini-embedding-2` | Google only |
| `eveli.metis.google-genai.embedding.dimensions` | `1024` | Google only. Must equal `indexing.embedding-dimension`; a reindex fails fast otherwise |

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
Both also import `EveliAutoConfigMetisGoogleGenAi`, which creates the Gemini beans only for the
`spring.ai.model.*` keys set to `google-genai`. Locally, `gcloud auth application-default login`
provides the credentials.

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

For Vertex AI instead of Ollama:

```
EVELI_METIS_ENABLED=true
EVELI_METIS_SEARCH_ENABLED=true
SPRING_AI_MODEL_CHAT=google-genai
SPRING_AI_MODEL_EMBEDDING=google-genai
EVELI_METIS_GOOGLEGENAI_PROJECTID=<project>
EVELI_METIS_GOOGLEGENAI_LOCATION=eu
```

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
the embedding model (provider or model id) or bumping
`eveli.metis.search.indexing.metadata-prompt-version` already invalidates stored hashes, so a
normal reindex reprocesses those rows.

A switch of embedding model also starts that reindex by itself. The startup reindex runs when no
completed job used the current model, the live-publication check does not count jobs of another
model, and the portal serves keyword search until a job with the current model completes. Vectors
of two models have the same width, so without this pgvector would rank them against each other
without any error.

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

`application-dev.yml` has Metis off with both `spring.ai.model.*` on `ollama`. `spring-boot:run`
pins `spring.config.location` to the profile files only, so the base `application.yml` is not
read locally — keep the settings in `application-dev.yml`.

With Ollama, set `eveli.metis.enabled` and `eveli.metis.search.enabled` to true and start the
models too:

```bash
cd mvn_setup/eveli-parent/eveli-local-docker && docker compose up -d postgresql ollama ollama-init
```

`ollama-init` pulls `bge-m3` and `llama3.2` from `registry.ollama.ai` unless they are already in
`./_db/ollama_data` (large download on first run). Air-gapped machines must pre-seed that volume.
With CPU Ollama also set `eveli.metis.search.query.embed-concurrency: 1` (one query embed at a
time) and `eveli.metis.search.indexing.concurrency: 1`. Production leaves `embed-concurrency`
unset so it matches `query.concurrency` (4).

With Vertex AI, set both `spring.ai.model.*` to `google-genai`, enable the flags and uncomment
the `eveli.metis.google-genai.*` block at the end of `application-dev.yml`, with your project id.
Only PostgreSQL is needed (`docker compose up -d postgresql`). Start with
`GOOGLE_APPLICATION_CREDENTIALS` pointing at a `metis-vertex` key, or after
`gcloud auth application-default login` (see [GCP setup](#gcp-setup)).

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

Ranking, the document model, and the empty-query floor are in
[IMPLEMENTATION.md](docs/site-search/IMPLEMENTATION.md). The threshold values per embedding model
are in [Score thresholds](#score-thresholds).

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
