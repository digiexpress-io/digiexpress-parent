# About

This document contains list of configuration properties and describes their usage.



# Standard Spring Boot configuration properties

## Database connection properties

* `spring.datasource.url` - JDBC URL of the database. Database should be PostgreSQL, version 16.
* `spring.datasource.username` - Login username of the database.
* `spring.datasource.password` - Login password of the database.

## Active profiles

* `spring.profiles.active` - this depends from actual eveli application, Digiexpress example application uses `jwt` profile to use JWT tokens from gateways to get user and authorization information. 

## Logging configuration

* `logging.config` - location for file defining logback logging configuration.
* `logging.level.*` - changing.logging level of specific loggers (in specific classes)


# Eveli configuration properties

Here are given properties which depend from environment and require customization. For full list of configuration properties information see classes in package `io.digiexpress.eveli.client.config`

## Endpoints to other services

* `eveli.dialob.service-url`: URL of dialob service, for both API and session. This assumes that there is some proxy or load balancer, which handles requests to this endpoint and routes them between dialob API and dialob session service. Actual url-s to services are obtained by adding assumed `dialob/api` and `session/dialob` paths to this service URL.
* `eveli.dialob.api-url`, `eveli.dialob.session-url`: optional API and session URL if they are in in different addresses, overriding `eveli.dialob.service-url`.

* `eveli.crm.host`: URL for portal gateway, which services client (person or company roles). Current implementation uses Suomi.fi authorization service to obtain authorization roles. 

* `eveli.org.service-url`: URL for organization service, providing mapping from group names to group member email addresses. Typically it is eveli gateway, providing this service.

* `eveli.printout.service-url`: URL for printout service, providing PDF for dialob forms. This service expects input JSON in following format:
``` 
{"lang":"en", "form":{...dialob form...}, "session":{...dialob session...}}
```
* `eveli.feedback.analyzer.endpoint-url`: feedback analyzer URL for providing sentiment of feedback. This is based on text analyze of feedback.

* `eveli.attachment-config.download-bucket`: bucket name in cloud environment to store task attachments. Digiexpress contains example implementation for Google Cloud.

* `eveli.tagomi.service-url`: service URL for next generation PDF generator. This uses custom templates created in Digiexpress.

## Security properties

* `eveli.jwt.eveli-public-key-value`, `eveli.jwt.eveli-issuer`: RSA public key and issuer for validating backend user tokens. These are used to validate JWT tokens from backend gateway.

* `eveli.jwt.gamut-public-key-value`, `eveli.jwt.gamut-issuer`: RSA public key and issuer for validating portal user tokens.

## Suomi.fi notification service properties
* `eveli.suomifi.rest.enabled`: boolean value to enable/disable Suomi.fi notification service  
* `eveli.suomifi.rest.service-id`: service client ID 
* `eveli.suomifi.rest.password`:  service client password
* `eveli.suomifi.rest.endpoint`: endpoint for Suomi.fi service

## Email service properties
* `eveli.email.enabled`: boolean to enable/disable email sending. 
* `eveli.email.host-name`: email server's host name 
* `eveli.email.host-port`: email server's port
* `eveli.email.sender-email`: sender email address
* `eveli.email.sender-name`: name of sender
* `eveli.email.server-user-name`: email server login user
* `eveli.email.server-password`: email server login password
* `eveli.email.allowed-recipients`: list of specific users to whom email sending is allowed
* `eveli.email.enabled-domains`: list of specific email address domain to whom email sending is allowed

## Service properties

* `eveli.feedback.enabled` - boolean flag to enable feedback functionality. 

## Metis AI platform properties

See `mvn_setup/metis-parent/README.md` for the architecture. Semantic site search is the first capability.

The platform flag alone is a valid deployment; it wires the models and the status endpoint but no capability.

* `eveli.metis.enabled`: boolean flag to enable the platform. Requires `spring.ai.model.*` below, since Metis needs an `EmbeddingModel` and a `ChatClient` bean. Enables `GET /worker/rest/api/metis/status`, which reports the resolved provider and models plus one entry per capability with its own state.

### AI model properties

Metis uses Spring AI, so the provider is chosen entirely through configuration. The defaults keep Spring AI from creating any model bean:

* `spring.ai.model.chat`, `spring.ai.model.embedding`: set both to `ollama` when enabling Metis, `none` otherwise.
* `spring.ai.ollama.base-url`: Ollama endpoint, `http://localhost:11434` locally. On GCloud this must be overridden (`SPRING_AI_OLLAMA_BASE_URL`); leaving the YAML default talks to localhost inside the container.
* `spring.ai.ollama.embedding.options.model`: embedding model, `bge-m3`.
* `spring.ai.ollama.chat.options.model`, `spring.ai.ollama.chat.options.temperature`: model used to generate human readable descriptions during indexing, `llama3.2` at a low temperature.

## Metis semantic site search properties

Semantic site search indexes the deployed portal content into PostgreSQL and serves the portal search from it. It is off by default: with `eveli.metis.search.enabled` unset no search beans are created and the portal keeps using its client-side keyword search. It needs `eveli.metis.enabled` as well, for the models. See `mvn_setup/metis-parent/README.md#semantic-site-search` for the setup.

* `eveli.metis.search.enabled`: boolean flag to enable the capability. Requires `eveli.metis.enabled` as well; enabling search alone fails boot with an explicit error. Flyway always creates the search tables (`db/postgresql/V4_1__metis_search.sql`) with built-in types, so vanilla PostgreSQL still boots while this flag is off. Turning the flag on runs an idempotent ensure for `vector`, `pg_trgm` and `unaccent`, the `embedding VECTOR(1024)` column, and HNSW/trigram indexes. That needs pgvector binaries (self-hosted: switch to `pgvector/pgvector:pg17` **keeping the existing volume**) or a DBA to `CREATE EXTENSION` (typical on Cloud SQL).
* `eveli.metis.search.locales`: comma-separated locales to index, defaults to `en, fi, sv`. Only these three have a PostgreSQL stemmer, other locales are indexed without stemming.
* `eveli.metis.search.auto-reindex-on-startup`: start an indexing job on boot when the index is still empty. Defaults to true (Java and YAML). This is how the first index is built in a deployed environment, no manual call needed. Set it false if you enable search only via env and do not want a boot-time job.
* `eveli.metis.search.reindex-on-deployment`: reindex after content is deployed or a scheduled publication goes live, defaults to true. Documents whose content hash is unchanged cost nothing.
* `eveli.metis.search.live-publication-check-seconds`: how often to check whether a scheduled publication has gone live, default `60`. Also the catch-up after a restart that missed the instant.

Query tuning, the defaults are the values the evaluation harness was tuned against:

* `eveli.metis.search.query.vector-weight` / `eveli.metis.search.query.fts-weight`: weights of the semantic and keyword rankings when they are fused, defaults `0.8` and `0.2`.
* `eveli.metis.search.query.rrf-k`: reciprocal rank fusion constant, default `60`. Higher values flatten the influence of the top ranks.
* `eveli.metis.search.query.default-limit` / `eveli.metis.search.query.max-results`: both default to `8`. The caller cannot ask for more than `max-results`. Keep them equal or callers silently get fewer rows than they asked for.
* `eveli.metis.search.query.min-results-before-fallback`: below this number of keyword hits the trigram similarity fallback is used, which catches typos, default `1`.
* `eveli.metis.search.query.trgm-threshold`: minimum trigram similarity for that fallback, default `0.2`.
* `eveli.metis.search.query.score-drop-off-ratio`: drop vector hits scoring below this fraction of the best match for the same query, default `0.90`. Not applied to a keyword-only list, so form-name hits stay.
* `eveli.metis.search.query.min-vector-score`: when even the best vector match is below this, the query has no semantic answer, default `0.30`.
* `eveli.metis.search.query.min-vector-score-without-keyword`: when primary full-text search is empty, a best vector score below this is treated as no answer, default `0.45`. Gibberish typically scores 0.30–0.45. Does not apply when the query matched a title or other `websearch_to_tsquery` hit. Does not fall through to the trigram keyword fallback.
* `eveli.metis.search.query.max-query-chars`: public queries longer than this are truncated, default `400`.
* `eveli.metis.search.query.timeout-seconds`: upper bound for answering one search, default `5`. Query embedding is given two seconds less than this; if it misses, hybrid still returns keyword ranking instead of failing.
* `eveli.metis.search.query.concurrency` / `eveli.metis.search.query.queue-capacity`: bounded search executor, defaults `4` and `50`. A full queue answers `fallback: true` without embedding.
* `eveli.metis.search.query.embed-concurrency`: how many query embeddings may be in flight in the JVM. Unset means the same as `query.concurrency` (so production overlapping searches still get semantic ranking). Set `1` on CPU-only Ollama (`application-dev.yml` does). When the cap is hit, a *different* uncached query fails fast and hybrid continues as keyword ranking; the same string joins the in-flight embed. Index-time `embed()` is not gated. `bge-m3` on one Ollama process still serializes internally; this cap is how many HTTP embeds we are willing to have in that queue so the last still finishes inside `timeout-seconds`.
* `eveli.metis.search.query.rate-limit-requests` / `eveli.metis.search.query.rate-limit-window-seconds`: in-memory per remote address, default 10 requests / 10s. Over-limit also answers `fallback: true`.
* Portal `GET /portal/site/search` answers `fallback: true` while the latest reindex job is `RUNNING`, `CANCELLING`, `FAILED`, or `CANCELLED`, so visitors keep keyword search until a job `COMPLETED`. `GET /worker/rest/api/metis/search/status` shows the failed or cancelled job so an operator can re-run reindex.

Indexing tuning:

* `eveli.metis.search.indexing.embedding-dimension`: documents the expected width of `metis_search_index.embedding`, `1024` for `bge-m3`. It does **not** alter the schema; the column is added as `VECTOR(1024)` when search is first enabled. A mismatch fails the reindex job. Changing the width is a schema concern.
* `eveli.metis.search.indexing.concurrency`: documents processed in parallel, default `1`, which is optimal for CPU-only Ollama. Raise it when Ollama runs on a GPU.
* `eveli.metis.search.indexing.document-timeout-seconds`: upper bound for metadata plus embedding of one document, measured from when that document starts (not from when it was queued), default `600`.
* `eveli.metis.search.indexing.abandoned-after-seconds`: how long a running job may report no progress before another instance may reclaim it, default `3600`.
* `eveli.metis.search.indexing.max-llm-context-chars`, `eveli.metis.search.indexing.max-page-chars`: truncation limits for the text handed to the metadata model and for the indexed page body.
* `eveli.metis.search.indexing.generic-topic-threshold`: a topic linking **more than** this many workflows (so 5 or more at the default of 4) is treated as a generic listing page. Its page text, headings and sibling link titles are dropped from each workflow document. The topic title is still used as the document category and is still handed to the metadata model.
* `eveli.metis.search.indexing.metadata-prompt-version`: folded into the content hash. Bump it when the prompt changes so a normal reindex reprocesses every document.

The embedding model id is folded into each document's content hash, so switching `spring.ai.ollama.embedding.options.model` and running a normal reindex reprocesses every row. `POST /worker/rest/api/metis/search/reindex?force=true` is still there for anything the hash cannot see. `POST /worker/rest/api/metis/search/reindex?replace=true` stops a running job and starts a new one; `POST /worker/rest/api/metis/search/reindex/cancel` stops without starting another. The model id is recorded on the job and is visible through `GET /worker/rest/api/metis/search/status`. A job is not finished because `state` is `COMPLETED` if `processedCount + skippedCount` is less than `totalCount` — that used to happen when queued documents hit the timeout together; that path now fails the job and leaves the previous index in place.

## Backend configuration properties

* `eveli.envir.dev-enabled` - "true" to show assets with dev mode enabled in portal.
* `eveli.tenant-features`- comma-separated list of UI and backend features. 
List of features:
- wrench-disabled : wrench UI is not available, usable in production environments where wrench uses fixed version.
- stencil-disabled: stencil UI is available, usable in production environment where stencil uses fixed version. 
- external-deployment
- queues-visually-disabled: queues UI is not available
- batches: batches are enabled
- batches-dev: development batches are enabled
- metis: Metis status and reindex page is shown in the worker UI