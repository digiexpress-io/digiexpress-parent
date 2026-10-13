_last updated: 13.10.2026_

# Adding a New Data Field to fs-composer

This document covers the end-to-end process of adding a new editable field to the `fs-composer` module. Follow the layers in order: database schema → persistence entity → domain model → REST endpoint → fetch hook → provider context → type definitions → UI component.

The two reference fields throughout this document are `assetDescription` (a scalar `TEXT` field) and `labels` (an array-valued `JSONB` field).

---

## Layer 1: Database Schema — `PropsTable`

File: `thena-fs-client/src/main/java/io/resys/thena/fs/tables/PropsTable.java`

### DDL

The `props` table holds all editable metadata for filesystem nodes:

```sql
CREATE TABLE {props} (
  props_id          UUID    PRIMARY KEY,
  props_description TEXT,
  props_labels      JSONB,
  props_comments    JSONB,
  props_permissions JSONB,
  props_flags       JSONB
);
```

`props_description` is `TEXT`. All other columns are `JSONB`. Use `TEXT` for scalar string values. Use `JSONB` for any structured, array, or map value.

### Content-addressable identity

`props_id` is not a sequence. It is a hash of all column values (see Layer 2). Two rows with identical field values produce the same `props_id` and collapse to one row via:

```sql
INSERT INTO {props} (...) VALUES (...) ON CONFLICT (props_id) DO NOTHING
```

This means a new column **must** participate in the hash computation in `Props.newInstance(...)`. Failing to include it breaks deduplication: two logically different rows would produce the same id and one would be silently dropped.

### Row mapping

`PropsMapper` reads each column and wraps it in `Optional.ofNullable(...)`. `PropsInsertMapper` writes each field back with `.orElse(null)` to translate absent optionals to SQL `NULL`.

When adding a column:
1. Add it to the DDL string with the correct type (`TEXT` or `JSONB`).
2. Add a `COMMENT ON COLUMN` line.
3. Add it to the `INSERT` column list and `VALUES(...)` positional list.
4. Read it in `PropsMapper.apply(Row)` using `row.getString(...)` for `TEXT` or `row.getJsonObject(...)` for `JSONB`, wrapped in `Optional.ofNullable(...)`.
5. Write it in `PropsInsertMapper.apply(Props)` as the next positional parameter.

---

## Layer 2: Persistence Entity — `Props`

File: `thena-fs-client/src/main/java/io/resys/thena/fs/entities/Props.java`

### Interface fields

```java
@Value.Immutable
@JsonSerialize(as = ImmutableProps.class)
@JsonDeserialize(as = ImmutableProps.class)
public interface Props extends Entity {
  UUID getId();
  Optional<String> getPropsDescription();
  Optional<JsonObject> getPropsLabels();
  Optional<JsonObject> getPropsComments();
  Optional<JsonObject> getPropsPermissions();
  Optional<JsonObject> getPropsFlags();

  @Value.Auxiliary @Nullable PropsTransitives getTransitives();
}
```

All data fields are `Optional<>`. `PropsTransitives` carries audit timestamps (`createdAt`, `updatedAt`) and is annotated `@Value.Auxiliary` so Immutables excludes it from equality and hash computation.

### Hash computation in `newInstance`

```java
// H(props) = μ(props_description ⊕ props_labels ⊕ props_comments ⊕ props_permissions ⊕ props_flags)
public static ImmutableProps.Builder newInstance(
    String description,
    JsonObject labels,
    JsonObject comments,
    JsonObject permissions,
    JsonObject flags
  ) {
  final var content = Entity.uuid();
  content.append(description != null ? description : "null");
  content.append(labels != null ? Blob.canonicalizeJson(labels) : "null");
  // ... same for comments, permissions, flags
  return ImmutableProps.builder()
      .id(content.build())
      .propsDescription(Optional.ofNullable(description))
      // ...
}
```

Each field is appended to the content buffer in a fixed order. `Blob.canonicalizeJson(...)` sorts JSONB keys before hashing to make the hash order-independent.

When adding a new field:
1. Add a getter to the `Props` interface: `Optional<String> getPropsMyField()` or `Optional<JsonObject> getPropsMyField()`.
2. Add a parameter to `newInstance(...)`.
3. Append the new field to `content` in the same fixed position as its parameter.
4. Set it on the builder: `.propsMyField(Optional.ofNullable(myField))`.

The parameter order in `newInstance`, the `content.append(...)` order, and the `PropsInsertMapper` positional parameter order must all match.

---

## Layer 3: Domain Model and Authoring — `WorldFsProps`, `ModifyDescription`, `ModifyLabels`

Files under `limaone-compiler/src/main/java/io/resys/limaone/`:
- `fs/WorldFsProps.java`
- `authoring/ModifyDescription.java`
- `authoring/ModifyLabels.java`
- `persistence/ModifyDescriptionImpl.java`
- `authoring/Authoring.java`

### `WorldFsProps` — domain view

`WorldFsProps` is the domain-model interface that callers read. It exposes field values after the persistence layer has resolved them. Optional fields use `@Nullable` (not `Optional<>`):

```java
public interface WorldFsProps {
  String getId();
  BodyType getType();
  Boolean getLocked();

  @Nullable String getAssetDescription();   // scalar, nullable
  List<DescriptionLabel> getLabels();        // array, never null (returns empty list)
  // ...
}
```

Add a new field here with `@Nullable ReturnType getMyField()` for optional scalars, or `List<MyType> getMyField()` for array-valued fields.

### Modify-props command — `ModifyDescription` as the reference

Each editable field has a matching `Modify*` interface:

```java
public interface ModifyDescription {
  ModifyDescription props(ModifyDescriptionProps props);
  ModifyDescription props(Consumer<ImmutableModifyDescriptionProps.Builder> props);
  Uni<Model<?>> build();
  Model<?> buildSync();

  @Value.Immutable
  @JsonSerialize(as = ImmutableModifyDescriptionProps.class)
  @JsonDeserialize(as = ImmutableModifyDescriptionProps.class)
  interface ModifyDescriptionProps extends AuthoringModelProps {
    String getId();        // dirent id — required on every *Props
    @Nullable String getText();  // the new value
  }
}
```

`ModifyLabelsProps` follows the same shape with `List<DescriptionLabel> getValues()` instead of `@Nullable String getText()`.

The minimum shape every `*Props` inner type must have:
- `String getId()` — the dirent id
- one or more value getters for the field being mutated

### `ModifyDescriptionImpl` — persistence

```java
public Uni<Model<?>> build() {
  return config.getPersistence().worldBuilder()
    .createdAt(getCreatedAt())
    .author(getAuthor())
    .docsId(props.getId())
    .build(nextWorld -> {
      return nextWorld.mergeModel(props.getId(), ImmutableDescription.builder().text(props.getText()).build());
    });
}
```

The impl calls `nextWorld.mergeModel(id, ImmutableMyModel.builder()...build())`. Add a corresponding `ImmutableMyField` model class and call `mergeModel` the same way.

### Registration in `ModifyModel`

```java
interface ModifyModel {
  ModifyDescription modifyDescription();
  ModifyLabels modifyLabels();
  // ... other modify methods
}
```

Add `ModifyMyField modifyMyField();` to `ModifyModel` and provide the implementation in `AuthoringImpl`.

---

## Layer 4: REST Endpoint — `AssetsFsController`

File: `eveli-client/src/main/java/io/digiexpress/eveli/client/web/resources/assets/AssetsFsController.java`

### Base path and naming convention

```java
@RestController
@RequestMapping("/worker/rest/api/assets/fs")
```

Per-field endpoints follow the pattern `PUT /dirents/{fieldName}/{id}`. Existing examples:

| Field | Endpoint |
|---|---|
| description | `PUT /dirents/description/{id}` |
| labels | `PUT /dirents/labels/{id}` |
| name | `PUT /dirents/name/{id}` |

### Method signature

```java
@PutMapping("dirents/description/{id}")
public Uni<Model<?>> updateDescription(
    @PathVariable("id") String id,
    @RequestBody ModifyDescriptionProps body)
{
  return authoring.modifyModel().modifyDescription().props(body).build();
}
```

The controller adds no logic. It passes `body` directly to `authoring.modifyModel().modifyMyField().props(body).build()`. The `@PathVariable String id` is present for REST legibility but is not separately extracted — the `id` inside `body` is what the impl uses.

Add one `@PutMapping` method per new field. Return type is `Uni<Model<?>>`.

---

## Layer 5: Frontend Fetch Hook — `eveli-api`

Files: `ts_setup/modules/eveli-api/fetch/`

### File naming convention

Fetch hook files are named after the route they call, with `$` marking path parameters:

```
worker.rest.api.assets.fs.dirents.description.$id.put.ts
worker.rest.api.assets.fs.dirents.labels.$id.put.ts
```

For a new field `myField`, create:

```
worker.rest.api.assets.fs.dirents.myField.$id.put.ts
```

### `createFileFetch` pattern

```typescript
export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/description/$id.PUT')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const baseline = React.useCallback(async (id: string, body: {}) => {
    return params.fetch(url({id}), {
      method,
      headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    .then((data) => console.log("update description:", data))
    .catch(error => {
      enqueueSnackbar(intl.formatMessage({ id: 'error.saveFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
    });
  }, [params]);

  return {
    putDescription: async (props: { id: string, text?: string }): Promise<void> => {
      await baseline(props.id, { id: props.id, text: props.text });
    }
  }
}
```

- The route key passed to `createFileFetch(...)` must match the controller path, with `$id` as the parameter placeholder and `.PUT` as the method suffix.
- `url({id})` substitutes the `$id` segment.
- `JSON.stringify(body)` serializes the request body. The body shape must match the `*Props` interface on the backend (at minimum: `id` and the value field).
- The named export from `hook` matches the operation: `putDescription`, `putLabels`, `putMyField`.

---

## Layer 6: Frontend Provider Context — `FsDirentProvider`

File: `ts_setup/modules/fs-api/fs-provider/FsDirentProvider.tsx`

### `FsDirentContextType` — public API

Add the new context method:

```typescript
export interface FsDirentContextType {
  // existing
  updateDirentDescription(id: string, text?: string): Promise<void>;
  updateDirentLabels(id: string, values: Fs.DescriptionLabel[]): Promise<void>;

  // new
  updateDirentMyField(id: string, value: MyValueType): Promise<void>;
}
```

### `FsDirentProviderProps.persistenceUnit` — injection point

Add the corresponding push function:

```typescript
export interface FsDirentProviderProps {
  persistenceUnit: {
    // existing
    pushDescription: (props: { id: string; text?: string }) => Promise<void>;
    pushLabels: (props: { id: string; values: Fs.DescriptionLabel[] }) => Promise<void>;

    // new
    pushMyField: (props: { id: string; value: MyValueType }) => Promise<void>;
  }
}
```

The consuming application wires the fetch hook's named return into this slot.

### `_initCtx` — implementation

Follow the exact pattern of `updateDirentDescription` and `updateDirentLabels`:

```typescript
async function updateDirentMyField(id: string, value: MyValueType): Promise<void> {
  const bodyType = dirents.getDirent(id)?.type ?? 'ARTICLE';
  try {
    await persistenceUnit.pushMyField({ id, value });
    const bodyName = dirents.getDirentName(id) ?? id;
    notify({ id: 'fs.snackbar.saveSuccess', bodyName, bodyType });
    refresh();
  } catch (error: any) {
    notify({ id: 'fs.snackbar.saveFailed', error, bodyType });
  }
}
```

Sequence: call `persistenceUnit.push*`, emit success snackbar via `notify`, then call `refresh()` to re-fetch dirents. On error: emit failure snackbar. Do not `await refresh()` in the success path (matches existing pattern for description and labels).

---

## Layer 7: Type Definitions — `Fs.PropsBase` and `Fs.DirentBase`

File: `ts_setup/modules/fs-api/fs-types.ts`

### `Fs.PropsBase`

Add the new field here so all type-specific props interfaces inherit it automatically:

```typescript
export interface PropsBase {
  id: string;
  bodyType: BodyType;
  expanded: boolean;
  reference: boolean;
  locked: boolean;
  assetDescription?: string;         // optional scalar
  labels: DescriptionLabel[];        // array, always present
  configOptions: ConfigOption[];
  comments: Comment[];
  changes: Change[];
  permissions: Permission[];
  errors: AssetError[];

  myField?: string;                  // new optional scalar
  // OR
  myArrayField: MyType[];            // new array field
}
```

Add optional scalar fields with `?`. Add array fields without `?` (initialize to `[]` in backend mapping).

### `Fs.DirentBase`

No change required. `DirentBase.props` is typed as `PropsBase | undefined` and all specific props interfaces extend `PropsBase`, so the new field is available everywhere `props` is accessed.

### `Fs.DescriptionLabel`

The shape used by `labels`:

```typescript
export interface DescriptionLabel {
  key: string;
  text?: string;
}
```

If the new field uses a structured element type, define a new interface in `fs-types.ts` following this pattern.

---

## Layer 8: UI Component — fs-composer

Directory: `ts_setup/modules/fs-composer/fs-dirent-{fieldName}/`

Each field-editing panel lives in its own directory with four files.

### File 1: `FsDirentMyFieldProps.ts`

```typescript
import { Fs } from "@dxs-ts/fs-api";

export interface FsDirentMyFieldProps {
  dirent: Fs.DirentBase;
}
```

### File 2: `useOwnerState.ts`

Scalar field (follows `fs-dirent-description`):

```typescript
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentMyFieldProps } from './FsDirentMyFieldProps';
import React from 'react';

export interface OwnerState {
  myField: string;
  isDirty: boolean;
  onChangeMyField: (value: string) => void;
  onSave: () => Promise<void>;
}

export const useOwnerState = (props: FsDirentMyFieldProps): OwnerState => {
  const { dirent } = props;
  const { updateDirentMyField } = useFsDirent();
  const init = dirent.props?.myField ?? '';
  const [myField, setMyField] = React.useState(init);

  function onChangeMyField(value: string) {
    setMyField(value);
  }

  async function onSave() {
    await updateDirentMyField(dirent.id, myField);
  }

  return {
    myField,
    isDirty: myField !== init,
    onChangeMyField,
    onSave,
  };
};
```

Array field (follows `fs-dirent-labels`): initialize from `dirent.props?.myArrayField ?? []`, compute `isDirty` with `JSON.stringify(current) !== JSON.stringify(init)`.

Key rules:
- Initialize local state from `dirent.props?.fieldName` — never from a separate `useState` with a hardcoded default while the real value is in `_ChangeState`.
- `init` is captured once at hook construction. `isDirty` compares current state against `init`.
- `onSave` calls the context method directly. No local loading state.

### File 3: `useUtilityClasses.ts`

```typescript
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentMyField';

export interface FsDirentMyFieldClasses {
  root: string;
  title: string;
  titleRow: string;
}

export type FsDirentMyFieldClassKey = keyof FsDirentMyFieldClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleRow: ['titleRow'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentMyFieldRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  // slot styles using FsColors and theme.spacing
}));
```

- `MUI_NAME` is the PascalCase component name. It is the prefix for all generated class names.
- Never use `sx` props. All styling goes in the styled component.
- Never use `display: grid`. Use `display: flex` with fixed widths.

### File 4: `FsDirentMyField.tsx`

```typescript
import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentMyFieldProps } from './FsDirentMyFieldProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentMyFieldRoot } from './useUtilityClasses';
import { FsDirentButtonSave } from '../fs-dirent-button-save';

export const FsDirentMyField: React.FC<FsDirentMyFieldProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentMyFieldRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>
          {intl.formatMessage({ id: 'fs.dirent.myFieldField.label' })}
        </Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>

      {/* field input rendered here */}
    </FsDirentMyFieldRoot>
  );
};
```

- `FsDirentButtonSave` receives `disabled={!ownerState.isDirty}`. The save button is inert until the field value differs from the initial snapshot.
- Pass `ownerState` to the root component only if the styled component needs it for conditional styling.

---

## Ambiguities Worth Flagging

**`updateDescription` method name collision in `AssetsFsController`**: The method handling `PUT /dirents/labels/{id}` is also named `updateDescription` (line 223). This is a copy-paste error — it should be `updateLabels`. It does not affect runtime behavior because Spring routes by path, not method name, but it will cause confusion when reading the controller.

**`refresh()` not awaited in description and labels update paths**: `updateDirentDescription` and `updateDirentLabels` in `_initCtx` call `refresh()` without `await`, while `updateDirent`, `copyDirent`, and `createDirent` do `await refresh()`. This means the UI may re-render before the dirent list is updated. Whether this is intentional is not documented. Match the existing pattern for the new field to be consistent; flag it to the team if strong consistency guarantees are needed.

**`PropsTable.findAllById` SQL bug**: The query uses `WHERE props.id = ANY($1)` but the column is `props_id`, not `id`. This query will fail at runtime. The bug is pre-existing and out of scope for field additions, but worth reporting.
