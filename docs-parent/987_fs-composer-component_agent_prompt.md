# Agent Prompt: fs-composer Component

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Task / Goal

Document the end-to-end process of adding a new data field to the `fs-composer` module. Read the backend and frontend files listed under "What to investigate", then write a single documentation file to `digiexpress-parent/docs-parent/` that covers all five layers in order: database schema, thena-fs persistence entity, limaone model, REST endpoint, and fs-composer UI component. The output must be a reference a developer can follow top-to-bottom when adding a field.

## What to investigate

### 1. Database schema — `PropsTable`

Read `digiexpress-parent/mvn_setup/thena-parent/thena-fs-client/src/main/java/io/resys/thena/fs/tables/PropsTable.java`.

Identify:
- The DDL for the `props` table and its column names (`props_description`, `props_labels`, `props_comments`, `props_permissions`, `props_flags`).
- How `Props.newInstance(...)` hashes column values to produce `props_id` (content-addressable identity).
- The `INSERT ... ON CONFLICT (props_id) DO NOTHING` deduplication pattern.
- Which columns are `TEXT` versus `JSONB` and why that matters when adding a new field.

### 2. Persistence entity — `Props`

Read `digiexpress-parent/mvn_setup/thena-parent/thena-fs-client/src/main/java/io/resys/thena/fs/entities/Props.java`.

Identify:
- The Immutables `@Value.Immutable` interface fields and their `Optional<>` wrappers.
- How `PropsTransitives` (audit timestamps) is attached via `@Value.Auxiliary`.
- Where a new field must be added to keep `Props.newInstance(...)` consistent with the hash computation.

### 3. Backend model and authoring — `WorldFsProps`, `ModifyDescription`, `ModifyLabels`

Read these files (all under `digiexpress-parent/mvn_setup/limaone-parent/limaone-compiler/src/main/java/io/resys/limaone/`):
- `fs/WorldFsProps.java` — the domain model view of props fields; `getAssetDescription()` and `getLabels()` are the reference examples.
- `authoring/ModifyDescription.java` and `authoring/ModifyLabels.java` — the command interface and its `*Props` inner type.
- `persistence/ModifyDescriptionImpl.java` — how the command calls `nextWorld.mergeModel(...)` with an `ImmutableDescription`.
- `authoring/Authoring.java` — how `ModifyModel` exposes `modifyDescription()` and `modifyLabels()` as named factory methods.

Identify:
- The `@Nullable` annotation pattern for optional fields in `WorldFsProps`.
- The Immutables `@Value.Immutable` + Jackson `@JsonSerialize`/`@JsonDeserialize` pattern on `*Props` interfaces.
- How `ModifyDescriptionProps` declares `getId()` and `getText()` — the minimum shape every modify-props command must have.
- Where a new modify-props command must be registered in `ModifyModel`.

### 4. REST endpoint — `AssetsFsController`

Read `digiexpress-parent/mvn_setup/eveli-parent/eveli-client/src/main/java/io/digiexpress/eveli/client/web/resources/assets/AssetsFsController.java`.

Identify:
- The base path `@RequestMapping("/worker/rest/api/assets/fs")` and the naming convention for per-field endpoints (e.g. `PUT /dirents/description/{id}`, `PUT /dirents/labels/{id}`).
- The method signature pattern: `@PutMapping`, `@PathVariable String id`, `@RequestBody *Props body`, return type `Uni<Model<?>>`.
- How the controller delegates directly to `authoring.modifyModel().*().props(body).build()` with no additional logic.

### 5. Frontend fetch hook — `eveli-api`

Read:
- `digiexpress-parent/ts_setup/modules/eveli-api/fetch/worker.rest.api.assets.fs.dirents.description.$id.put.ts`
- `digiexpress-parent/ts_setup/modules/eveli-api/fetch/worker.rest.api.assets.fs.dirents.labels.$id.put.ts`

Identify:
- The `createFileFetch(...)({ hook })` pattern and how the route key matches the controller path.
- The shape of the exported `Hook` object and the named return (`putDescription`, `putLabels`).
- How `url({id})` and `JSON.stringify(body)` are assembled for the request.

### 6. Frontend provider and context — `FsDirentProvider`

Read `digiexpress-parent/ts_setup/modules/fs-api/fs-provider/FsDirentProvider.tsx`.

Identify:
- `FsDirentContextType` — specifically `updateDirentDescription` and `updateDirentLabels` as the two reference operations.
- `FsDirentProviderProps.persistenceUnit` — `pushDescription` and `pushLabels` as the injection points where a consuming app wires in the fetch hooks.
- The `_initCtx` function — how each `updateDirent*` method calls `persistenceUnit.push*`, then calls `refresh()`, then emits a snackbar notification.

### 7. Frontend type definitions — `Fs.PropsBase` and `Fs.DirentBase`

Read `digiexpress-parent/ts_setup/modules/fs-api/fs-types.ts`.

Identify:
- `Fs.PropsBase` — all shared fields including `assetDescription` and `labels`.
- `Fs.DirentBase` — `id`, `name`, `type`, `fullPath`, `commitIndex`, `children`, `props`.
- How type-specific props interfaces (`ArticleProps`, `FlowProps`, etc.) extend `PropsBase`.
- `Fs.DescriptionLabel` — the `{ key: string; text?: string }` shape used by labels.

### 8. Representative fs-composer UI components

Read these files (two representative components that add a field-editing panel):

`digiexpress-parent/ts_setup/modules/fs-composer/fs-dirent-description/`:
- `FsDirentDescriptionProps.ts` — component input interface (`dirent: Fs.DirentBase`).
- `FsDirentDescription.tsx` — rendering: `FsDirentDescriptionRoot`, `useOwnerState`, `useUtilityClasses`, save button.
- `useOwnerState.ts` — `useFsDirent()` call, local state initialization from `dirent.props?.assetDescription`, dirty check, `onSave` calling `updateDirentDescription`.
- `useUtilityClasses.ts` — MUI utility classes + `FsDirentDescriptionRoot` styled component.

`digiexpress-parent/ts_setup/modules/fs-composer/fs-dirent-labels/`:
- `FsDirentLabels.tsx`, `useOwnerState.ts` — same pattern for labels; demonstrates array-valued fields and dirty check via `JSON.stringify` comparison.

Identify:
- The `OwnerState` interface shape: fields read from `dirent.props`, local state, `isDirty`, change handler, `onSave`.
- How `useOwnerState` initializes local state from `props` and compares against the initial snapshot to compute `isDirty`.
- The `FsDirentDescriptionRoot` styled component pattern and `MUI_NAME` constant.
- How `FsDirentButtonSave` is disabled until `isDirty` is true.

## Report

After completing, report:

**Status:** `COMPLETE`

- File written (path + prefix)
- Sections covered in the output document
- Any ambiguities found in the source code worth flagging to the developer
