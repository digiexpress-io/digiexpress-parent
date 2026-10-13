_last updated: 13.10.2026_

# Backend: Article, ArticleLink, and ArticleWorkflow Domain

Source module: `limaone-compiler`
Root package: `io.resys.limaone`

---

## Entity Fields

### `Article` (`model/Article.java`)

Implements `Model.Body`. All fields are stored under `BodyType.ARTICLE`.

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | `String` | yes | Display name; must be unique across all articles |
| `order` | `Integer` | yes | Sort position |
| `parentId` | `String` | no | ID of a parent `Article`; enables tree structure |
| `devMode` | `Boolean` | no | Omitted from JSON when null |
| `authOnly` | `Boolean` | no | Omitted from JSON when null |

`Article` does **not** carry any reference to `ArticleLink` or `ArticleWorkflow` IDs. The foreign key lives on the link/workflow side.

---

### `ArticleLink` (`model/ArticleLink.java`)

Implements `Model.Body`. All fields are stored under `BodyType.ARTICLE_LINK`.

| Field | Type | Required | Notes |
|---|---|---|---|
| `value` | `String` | yes | The URL or link target |
| `contentType` | `String` | yes | MIME-style content type (`type` field in props) |
| `articles` | `List<String>` | yes | List of `Article` IDs this link is associated to |
| `labels` | `List<LocaleLabel>` | yes | Locale-keyed display labels |
| `devMode` | `Boolean` | no | Omitted from JSON when null |

`ArticleLink` holds the foreign key: `articles` is a list of article IDs stored on the link record.

---

### `ArticleWorkflow` (`model/ArticleWorkflow.java`)

Implements `Model.Body`. All fields are stored under `BodyType.ARTICLE_WORKFLOW`.

| Field | Type | Required | Notes |
|---|---|---|---|
| `value` | `String` | yes | Service/workflow name (maps to `serviceName` in `ServiceProps`) |
| `articles` | `List<String>` | yes | List of `Article` IDs this workflow is associated to |
| `labels` | `List<LocaleLabel>` | yes | Locale-keyed display labels |
| `formId` | `String` | no | Dialob form UUID |
| `formName` | `String` | no | Dialob form name |
| `formTag` | `String` | no | Dialob form tag/version |
| `flowName` | `String` | no | Hdes flow name |
| `startDate` | `OffsetDateTime` | no | Validity start |
| `endDate` | `OffsetDateTime` | no | Validity end |
| `devMode` | `Boolean` | no | |
| `assignable` | `Boolean` | no | Maps to `ConfigOption.ASSIGNABLE_MODE` |
| `anon` | `Boolean` | no | Maps to `ConfigOption.ANONYMOUS_MODE` |
| `rps` | `Boolean` | no | Maps to `ConfigOption.RPS_MODE` |
| `disabled` | `Boolean` | no | Maps to `ConfigOption.DISABLED_MODE` |
| `inHouse` | `Boolean` | no | Maps to `ConfigOption.IN_HOUSE_MODE` |

`ArticleWorkflow` holds the foreign key: `articles` is a list of article IDs stored on the workflow record. `Article` holds no reference to workflows.

---

## Foreign Key Direction

Both `ArticleLink` and `ArticleWorkflow` carry an `articles: List<String>` field containing the IDs of the articles they belong to. `Article` itself stores neither link IDs nor workflow IDs.

This means:
- To find all workflows for an article, query `ModelWorld.getArticleWorkflows()` and filter by `articles.contains(articleId)`.
- To associate a workflow to an article, add the article's ID to the workflow's `articles` list.
- Deletion of an association (`DeleteArticleWorkflow`, `DeleteArticleLink`) removes the `articleId` from the respective record's `articles` list. It does **not** delete the workflow or link entity itself.

---

## Authoring Command Summary

All commands are defined as interfaces in `io.resys.limaone.authoring` and implemented in `io.resys.limaone.persistence`. They are accessed through `Authoring.NewModel`, `Authoring.ModifyModel`, and `Authoring.DeleteModel`.

### Article

| Command | Interface | Impl | Key Props |
|---|---|---|---|
| Create | `NewArticle` | `NewArticleImpl` | `name` (required), `id?`, `parentId?`, `order?`, `devMode?`, `authOnly?`, `assetDescription?`, `assetLabels?` |
| Modify | `ModifyArticle` | `ModifyArticleImpl` | `articleId` (required), `name`, `order`, `parentId?`, `links?`, `workflows?`, `devMode?`, `authOnly?`, `assetDescription?`, `assetLabels?` |
| Delete | via `DeleteAny` | `DeleteAnyImpl` | — |

`ModifyArticle.links` and `ModifyArticle.workflows` are optional full-replacement lists. When provided, the impl iterates all `ArticleLink` / `ArticleWorkflow` records in the world and adds or removes the `articleId` from each record's `articles` list to match the desired state.

### ArticleLink

| Command | Interface | Impl | Key Props |
|---|---|---|---|
| Create | `NewArticleLink` | `NewArticleLinkImpl` | `value` (required), `type` (required), `articles` (required), `labels` (required), `id?`, `devMode?`, `assetDescription?`, `assetLabels?` |
| Modify | `ModifyArticleLink` | `ModifyArticleLinkImpl` | `linkId` (required), `value`, `type`, `labels?`, `articles?`, `devMode?`, `assetDescription?`, `assetLabels?` |
| Disassociate from article | `DeleteArticleLink` | `DeleteArticleLinkImpl` | `linkId` (required), `articleId` (required) |

`DeleteArticleLink` removes `articleId` from `ArticleLink.articles`. If the article is not in that list, the operation is a no-op.

### ArticleWorkflow

| Command | Interface | Impl | Key Props |
|---|---|---|---|
| Create | `NewArticleWorkflow` | `NewArticleWorkflowImpl` | `value` (required), `articles` (required), `labels` (required), `formName` (required), `formTag` (required), `formId` (required), `flowName` (required), `id?`, `devMode?`, `anon?`, `disabled?`, `assignable?`, `rps?`, `startDate?`, `endDate?`, `assetDescription?`, `assetLabels?` |
| Modify | `ModifyArticleWorkflow` | `ModifyArticleWorkflowImpl` | `workflowId` (required), `value`, `formId?`, `formName?`, `formTag?`, `flowName?`, `labels?`, `articles?`, `disabled?`, `devMode?`, `anon?`, `rps?`, `assignable?`, `inHouse?`, `startDate?`, `endDate?`, `assetDescription?`, `assetLabels?` |
| Disassociate from article | `DeleteArticleWorkflow` | `DeleteArticleWorkflowImpl` | `workflowId` (required), `articleId` (required) |

---

## ArticleWorkflow: Create vs Modify Payload Differences

**Create (`NewArticleWorkflowProps`):**
- `formName`, `formTag`, `formId`, `flowName` are **required** (non-nullable).
- `articles` is **required** (non-nullable, validated immediately).
- `labels` is **required** (non-nullable).
- `anon` and `rps` default to `Boolean.TRUE.equals(...)`, so passing `null` stores `false` for these two fields only.
- `inHouse` is absent — not settable at creation time.

**Modify (`ModifyArticleWorkflowProps`):**
- All fields are nullable. Only `workflowId` and `value` are required.
- `formId`, `formName`, `formTag`, `flowName` are nullable; when null they overwrite the existing values with null (via `ImmutableArticleWorkflow.builder().from(start.getBody())` followed by explicit setters).
- `articles` and `labels` are full-replacement lists: when non-null, the impl resets the list to empty and rebuilds it. When null, the existing list is preserved (because Immutables `.from()` copies the original).
- `inHouse` is settable on modify but not on create.
- `assignable` is passed through directly on modify (no `Boolean.TRUE.equals` defaulting).

---

## Association Resolution

Both `NewArticleWorkflowImpl` and `NewArticleLinkImpl` accept article references that can be either an ID or a name. Resolution uses `ModelWorld.findOneArticle(articleRef)`, which checks by ID first, then by name (case-insensitive). The resolved ID is stored on the workflow/link record, not the original string reference.

The same resolution applies to locale references in `labels`: `ModelWorld.findOneLocale(localeRef)` resolves by ID first, then by locale code value. If the locale does not exist, `AuthoringException` is thrown.

---

## Filesystem Projection (`WorldFsProps`)

The `persistence/fs/Props_*Builder` classes convert stored model entities into `WorldFsProps` projections used by the filesystem layer.

| Entity | Builder | Projection Type | Notable mappings |
|---|---|---|---|
| `Article` | `Props_ArticleBuilder` | `ArticleProps` | `devMode` → `ConfigOption.DEV_MODE`; `authOnly` → `ConfigOption.AUTH_ONLY_MODE`; no link/workflow IDs projected |
| `ArticleLink` | `Props_ArticleLinkBuilder` | `LinkProps` | `value` → `urlValue`; `type` → `contentType`; `labels` → `intlValues` (locale → labelValue map); `articles` passed through |
| `ArticleWorkflow` | `Props_ArticleWorkflowBuilder` | `ServiceProps` | `value` → `serviceName`; `formName` → `dialobFormName`; `formTag` → `dialobFormTag`; `startDate`/`endDate` serialized to string; boolean flags → `ConfigOption` entries; `articles` passed through |

`ServiceProps` (the workflow projection) does **not** include `formId`. The form ID is stored on the model entity but is not projected into the filesystem representation.

---

## Error Handling

Authoring commands throw `AuthoringException` (unchecked) for:
- Referenced article not found by ID or name.
- Referenced locale not found.
- Referenced workflow or link not found by ID.
- Duplicate article name on modify.
- Article referencing itself as its own parent.

No HTTP-layer error mapping is present in this module. `AuthoringException` carries the originating props object for context.

---

## No REST Controllers in This Module

`limaone-compiler` contains no JAX-RS or Spring MVC controllers. It is a library module. REST exposure is handled by a consuming module (not present in the scanned path). The `Authoring` interface is the entry point for all mutations.

---

## Known Constraints

1. **`inHouse` is modify-only.** `NewArticleWorkflowProps` has no `getInHouse()`. A workflow can only be marked `inHouse` after creation via `ModifyArticleWorkflow`.

2. **`anon` and `rps` default differently at create vs modify.** At create time, `NewArticleWorkflowImpl` uses `Boolean.TRUE.equals(props.getAnon())`, so a null input stores `false`. At modify time, `ModifyArticleWorkflowImpl` passes `props.getAnon()` directly, so null overwrites with null (omits from JSON output).

3. **`articles` and `labels` on modify are full-replacement, not patch.** Passing a partial list replaces the entire existing list. Passing null preserves the existing list (`.from()` copy semantics).

4. **`DeleteArticleLink` and `DeleteArticleWorkflow` do not delete the entity.** They only remove the given `articleId` from the entity's `articles` list. The entity persists and remains available to other articles.

5. **`formId` is not projected into `ServiceProps`.** It is stored in the model (`ArticleWorkflow.formId`) and round-trips through create/modify, but `Props_ArticleWorkflowBuilder` does not write it to the filesystem projection.

6. **Article name uniqueness is enforced only on modify, not on create.** `NewArticleImpl` should be checked separately; `ModifyArticleImpl` explicitly throws if another article with the same name already exists.
