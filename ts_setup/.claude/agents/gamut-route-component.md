---
name: gamut-route-component
description: Creates a new page-level route component in gamut-routes, including utility classes, the route React component, optional types file and sub-components, barrel export, g-props registration, and TanStack Router route file wiring; invoke when a new application page or view is needed.
tools: Read, Grep, Glob, Edit, Write, Bash, Agent
---
_last updated: 13.10.2026_


# Agent Prompt: Create a New Gamut Route Component

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

Read `digiexpress-parent/docs-parent/999_ui_coding_standards.md` before writing any code. All rules in that file apply to this task.

Run `ls digiexpress-parent/docs-parent/` to see all available documentation files. Review the filenames and read any that appear to cover the module you are about to modify — use the directory listing as the authoritative index, not guessed names. If a relevant doc exists, read it in full before touching any files. If you are uncertain whether a file covers the module you are about to modify, ask before reading it or invoking the `exploration` agent — do not guess. If none exists, invoke the `exploration` agent first to produce one, read the file it created, then proceed.

## Required Input

The invoker must provide all of the following before work begins.

- **Component name** — the `g-router-*` directory name (e.g. `g-router-my-view`). Determines directory name, file names, `MUI_NAME`, props interface name, root styled component name, and class key prefix.
- **Purpose** — one sentence: what this route renders and which data it reads.
- **Route hierarchy** — which route file in `routes/` mounts this component. Either an existing file (e.g. `secured.$locale.views.$viewId.index.tsx`) or a new file (provide the full TanStack dot-separated path).
- **Auth context** — `public` (no auth required) or `secured` (requires `SecuredSetup` provider stack). Determines which providers are available.
- **Props** — the props the component receives from the route file. At minimum: `locale: string`. Secured view components also receive `viewId: GUserOverviewMenuView`.
- **Layout variant** — which `GLayout` variant the component uses (e.g. `secured-1-row-1-column`, `fill-session-start-end`, `toolbar-n-rows-2-columns`). If unknown, check `GLayout` in `@dxs-ts/gamut-primitives`.
- **Sub-components** — whether the component is simple (all JSX in one file) or complex (sub-components in separate files). For complex: list what sub-components are needed.
- **Navigation targets** — which routes the component navigates to. Determines which `from`/`to` paths to pass to `useNavigate`.

## Task

Create a new route component in `digiexpress-parent/ts_setup/modules/gamut-routes/` following the steps below exactly. A route component assembles primitives from `@dxs-ts/gamut-primitives`, API hooks from `@dxs-ts/gamut-api`, and shell infrastructure from `@dxs-ts/gamut-shell`. It does not contain form rendering or Dialob integration — those belong to `@dxs-ts/gamut-form`.

---

## Overview

Every route component has two layers:

1. **Utility classes layer** (`useUtilityClasses.ts` or `useUtilityClasses.tsx`) — defines `MUI_NAME`, the `Classes` interface, the `ClassKey` type, `useUtilityClasses()`, and the `GRouter<Name>Root` styled component. Also contains private sub-components (breadcrumbs, section titles) too small to warrant their own file.
2. **Route component** (`GRouter<Name>.tsx`) — the exported React component. Calls `useThemeProps` with `MUI_NAME`, calls `useUtilityClasses()`, and assembles `GShell`, `GAppBar`, `GLayout`, `GFooter`, and any view-specific primitives.

For complex routes, additional sub-component files (e.g. `GRouter<Name>Content.tsx`, `GRouter<Name>Actions.tsx`) sit alongside these two. A types file (`g-router-<name>-types.ts`) holds the `OwnerState` interface and its hook when state derivation logic does not belong in the main component.

A TanStack Router route file in `routes/` mounts the component. For secured views dispatched by `$viewId`, registration in `ChooseComponent` in `routes/secured.$locale.views.$viewId.index.tsx` is also required.

---

## File structure

| File | Purpose |
|---|---|
| `g-router-<name>/useUtilityClasses.ts` | `MUI_NAME`, `GRouter<Name>Classes`, `GRouter<Name>ClassKey`, `useUtilityClasses()`, `GRouter<Name>Root` styled component, private sub-components |
| `g-router-<name>/GRouter<Name>.tsx` | Exported route component. Props interface, `useThemeProps`, navigation handlers, JSX. |
| `g-router-<name>/g-router-<name>-types.ts` | `GRouter<Name>Props`, `GRouter<Name>OwnerState`, `useGRouter<Name>State` hook. Only present for complex routes with derived state. |
| `g-router-<name>/GRouter<Name><Sub>.tsx` | Additional sub-component files (breadcrumbs, content, actions). Only present for complex routes. |
| `g-router-<name>/index.ts` | Barrel export. |

For a simple route (e.g. `g-router-bookings`, `g-router-inbox`): three files — `useUtilityClasses.tsx`, `GRouter<Name>.tsx`, `index.ts`.

For a complex route (e.g. `g-router-product`, `g-router-secured-services`): five or more files including a types file and sub-component files.

---

## Step 1: Create the directory

```
digiexpress-parent/ts_setup/modules/gamut-routes/g-router-<name>/
```

---

## Step 2: Create useUtilityClasses

**File:** `g-router-<name>/useUtilityClasses.ts`

Use `.ts` for pure style/class files. Use `.tsx` when the file also contains private React sub-components (breadcrumbs, section headers).

```ts
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'GRouter<Name>';

export interface GRouter<Name>Classes {
  root: string;
  <name>TitleLayout: string;
  <name>Title: string;
  <name>SubTitle: string;
  <name>BodyText: string;
  <name>Breadcrumbs: string;
  // add one entry per distinct class key the component applies
}

export type GRouter<Name>ClassKey = keyof GRouter<Name>Classes;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    <name>TitleLayout: ['<name>TitleLayout'],
    <name>Title: ['<name>Title'],
    <name>SubTitle: ['<name>SubTitle'],
    <name>BodyText: ['<name>BodyText'],
    <name>Breadcrumbs: ['<name>Breadcrumbs'],
    // mirror every key in GRouter<Name>Classes
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const GRouter<Name>Root = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.<name>TitleLayout,
      styles.<name>Title,
      styles.<name>SubTitle,
      styles.<name>BodyText,
      styles.<name>Breadcrumbs,
      // list every key from GRouter<Name>Classes
    ];
  },
})(({ theme }) => {
  return {
    // CSS targeting `.GRouter<Name>-<slotName>` class names generated by composeClasses
    '.GRouter<Name>-<name>TitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center',
    },
    '.GRouter<Name>-<name>Title': {
      textAlign: 'center',
      ...theme.typography.h1,
    },
    '.GRouter<Name>-<name>SubTitle': {
      marginBottom: theme.spacing(1),
      ...theme.typography.h3,
    },
    '.GRouter<Name>-<name>BodyText': {
      ...theme.typography.body1,
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5),
    },
    '.GRouter<Name>-<name>Breadcrumbs': {
    },
  };
});
```

**Naming conventions:**
- `MUI_NAME`: `'GRouter<Name>'` — exact PascalCase; `GRouter` prefix always present.
- CSS class selector format inside styled: `.GRouter<Name>-<slotName>` (MUI class name generated by `generateUtilityClass`).
- `Classes` interface: `GRouter<Name>Classes`.
- `ClassKey` type: `GRouter<Name>ClassKey`.
- Root styled component: `GRouter<Name>Root`.

When the file is `.tsx`, add private sub-components after the styled component. They are not exported from `index.ts` but may be exported from `useUtilityClasses.tsx` for use in the main component file:

```tsx
import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { Home as HomeIcon } from '@mui/icons-material';
import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';
import { GUserOverviewMenuView } from '@dxs-ts/gamut-primitives';

// ... MUI_NAME, Classes interface, ClassKey type, useUtilityClasses(), GRouter<Name>Root styled component go here first ...

export const <Name>Title: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <Box className={classes.<name>TitleLayout}>
        <Avatar className={classes.avatar}>
          {/* icon */}
        </Avatar>
        <Typography className={classes.<name>Title}>{intl.formatMessage({ id: 'gamut.<name>.title' })}</Typography>
      </Box>
      <Typography className={classes.<name>BodyText}>{intl.formatMessage({ id: 'gamut.<name>.subtitle' })}</Typography>
    </>
  );
};

export const <Name>Breadcrumbs: React.FC = () => {
  const intl = useIntl();
  const nav = useNavigate();
  const classes = useUtilityClasses();

  function handleNav(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) {
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    });
  }

  return (
    <Breadcrumbs className={classes.<name>Breadcrumbs}>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
    </Breadcrumbs>
  );
};
```

---

## Step 3: Create the route component

**File:** `g-router-<name>/GRouter<Name>.tsx`

**Simple route (no extracted OwnerState):**

```tsx
import React from 'react';
import { Container, Divider, Drawer, useThemeProps } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';

import {
  GLayout,
  GFooter,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverviewMenu,
  G<Primitive>,
} from '@dxs-ts/gamut-primitives';
import { GShell, GShellClassName } from '@dxs-ts/gamut-shell';

import { <Name>Breadcrumbs, <Name>Title, GRouter<Name>Root, MUI_NAME } from './useUtilityClasses';
import { useUtilityClasses } from './useUtilityClasses';


export interface GRouter<Name>Props {
  locale: string;
  viewId: GUserOverviewMenuView;
  // add further props as needed
}

export const GRouter<Name>: React.FC<GRouter<Name>Props> = (initProps) => {
  const nav = useNavigate();
  const classes = useUtilityClasses();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },
      to: '/secured/$locale/views/$viewId',
    });
  }

  function handleClick(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) {
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    });
  }

  const breadcrumbs = React.useCallback(() => <<Name>Breadcrumbs />, []);
  const topTitle = React.useCallback(() => <<Name>Title />, []);

  return (
    <GShell>
      <GAppBar locale={props.locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={props.viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='<name>' />
      </Drawer>
      <main role='main'>
        <Container>
          <GRouter<Name>Root className={classes.root}>
            <GLayout variant='secured-1-row-1-column'
              slots={{
                breadcrumbs,
                topTitle,
                left: () => (
                  <>
                    <Divider />
                    <G<Primitive> />
                  </>
                ),
              }}
            />
          </GRouter<Name>Root>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
};
```

**Key rules:**
- `useThemeProps({ props: initProps, name: MUI_NAME })` is always the first call inside the component body (after hooks that do not depend on props).
- `useNavigate()` and `useUtilityClasses()` may be called before `useThemeProps`; all three appear unconditionally at the top of the component.
- For secured views, wrap content in `<GShell>`. For public views, use `<GShell drawerOpen={false}>`.
- `GAppBar` always receives `locale`, `onLocale`, and `onLogoClick`. Secured views also receive `viewId`.
- `GUserOverviewMenu` receives `onClick` (the navigation handler, named `handleClick`) and `defaultView` set to the `GUserOverviewMenuView` string for this view.
- Navigation handlers guard against `undefined` viewId with `if (!viewId) { return; }`.
- Access `props.locale` and `props.viewId` directly from the `useThemeProps` result — do not destructure them into local variables before use.
- `<main role='main'>` and `<footer role='footer'>` are always present.

**Public (unauthenticated) route variant:**

Public components do not mount `Drawer` or `GUserOverviewMenu`. They use `useLocale()` from `@dxs-ts/gamut-api` to read the current locale, and `useSite()` to read `views`. The `GRouterUnsecured` pattern splits `GShell` wrapping from logic by placing an `Internal` component inside the outer export:

```tsx
const Internal: React.FC<GRouter<Name>Props> = (initProps) => {
  const nav = useNavigate();
  const { locale } = useLocale();
  const { views } = useSite();
  const classes = useUtilityClasses();
  const ownerState = useThemeProps({ props: initProps, name: MUI_NAME });

  function handleLocale(locale: string) {
    nav({
      from: '/public/$locale',
      params: { locale },
      to: '/public/$locale',
    });
  }

  return (
    <>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => {}} />
      <main role='main'>
        <Container>
          <GRouter<Name>Root className={classes.root} ownerState={ownerState}>
            {/* content */}
          </GRouter<Name>Root>
        </Container>
      </main>
      <footer role='footer'>
        <GFooter />
      </footer>
    </>
  );
};

export const GRouter<Name>: React.FC<GRouter<Name>Props> = (props) => {
  return (
    <GShell drawerOpen={false}>
      <Internal {...props} />
    </GShell>
  );
};
```

**Complex route with extracted OwnerState:**

When the component derives state from multiple API hooks, extract to a types file (Step 4). The main component calls the state hook and passes `ownerState` to sub-components:

```tsx
import React from 'react';
import { Container, Toolbar } from '@mui/material';

import {
  GFooter,
  GLayout,
  GLogo,
} from '@dxs-ts/gamut-primitives';
import { GShell, GShellClassName } from '@dxs-ts/gamut-shell';

import { GRouter<Name>Root } from './useUtilityClasses';
import { GRouter<Name>Props, useGRouter<Name>State } from './g-router-<name>-types';
import { GRouter<Name>Breadcrumbs } from './GRouter<Name>Breadcrumbs';
import { GRouter<Name>Content } from './GRouter<Name>Content';
import { GRouter<Name>Actions } from './GRouter<Name>Actions';
import { useSite } from '@dxs-ts/gamut-api';


export const GRouter<Name>: React.FC<GRouter<Name>Props> = (props) => {
  const site = useSite();
  const isLoaded = Object.keys(site.views).length > 0;

  return (
    <GShell drawerOpen={false}>
      <Toolbar className={GShellClassName}>
        <GLayout variant='toolbar-n-rows-2-columns'>
          <GLogo variant='black_lg' />
        </GLayout>
      </Toolbar>

      <main role='main'>
        <Container>
          <GRouter<Name>Root>
            {isLoaded && <Layout {...props} />}
          </GRouter<Name>Root>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>
  );
};

const Layout: React.FC<GRouter<Name>Props> = (props) => {
  const { ownerState } = useGRouter<Name>State(props);
  return (
    <GLayout variant='fill-session-start-end'
      slots={{
        breadcrumbs: () => (<GRouter<Name>Breadcrumbs ownerState={ownerState} />),
        topTitle: () => <GRouter<Name>Content ownerState={ownerState} />,
        center: () => <GRouter<Name>Actions ownerState={ownerState} />,
      }}>
    </GLayout>
  );
};
```

---

## Step 4: Create the types file (complex routes only)

**File:** `g-router-<name>/g-router-<name>-types.ts`

```ts
import { IamApi, useIam, SiteApi, useSite } from '@dxs-ts/gamut-api';


export interface GRouter<Name>Props {
  productId: string;
  pageId: string;
  locale: string;
}


export interface GRouter<Name>OwnerState {
  ownerState: {
    topic: SiteApi.TopicView;
    topicLink: SiteApi.TopicLink | undefined;
    locale: string;
    isAnon: boolean;
    status: IamApi.FormLinkAuthType;
    // add fields as needed
  };
}


export function useGRouter<Name>State(props: GRouter<Name>Props): GRouter<Name>OwnerState {
  const iam = useIam();
  const site = useSite();

  const topic = site.views[props.pageId];
  const topicLink = topic.links.find(link => link.id === props.productId);
  const anonymousUser = iam.authType === 'ANON';

  const ownerState: GRouter<Name>OwnerState['ownerState'] = {
    topic,
    topicLink,
    locale: props.locale,
    isAnon: anonymousUser,
    status: iam.getFormLinkAuthType(topicLink),
  };

  return { ownerState };
}
```

When a types file exists, the props interface lives there — not in `GRouter<Name>.tsx`.

---

## Step 5: Create the barrel export

**File:** `g-router-<name>/index.ts`

```ts
export * from './GRouter<Name>';
export type { GRouter<Name>ClassKey, GRouter<Name>Classes } from './useUtilityClasses';
```

If a types file exists, also export from it (use `export *`, not a type-only export — the state hook function is a runtime export):

```ts
export * from './g-router-<name>-types';
```

Sub-component files (breadcrumbs, content, actions) are not exported from `index.ts` — they are used only within the sub-module.

---

## Step 6: Register in the top-level index.ts

**File:** `digiexpress-parent/ts_setup/modules/gamut-routes/index.ts`

Add one line following the existing alphabetical order among `g-router-*` exports:

```ts
export * from './g-router-<name>';
```

---

## Step 7: Register in g-props.ts

**File:** `digiexpress-parent/ts_setup/modules/gamut-routes/g-props/g-props.ts`

Make four additions.

**1. Import at the top (grouped with other router imports):**

```ts
import { GRouter<Name>ClassKey, GRouter<Name>Props } from '../g-router-<name>';
```

**2. Add to `GComponentsPropsList`:**

```ts
export interface GComponentsPropsList {
  // ...
  GRouter<Name>: GRouter<Name>Props;
  // ...
}
```

**3. Add to `GComponentNameToClassKey`:**

```ts
export interface GComponentNameToClassKey {
  // ...
  GRouter<Name>: GRouter<Name>ClassKey;
  // ...
}
```

**4. Add to `GComponents<Theme>`:**

```ts
export interface GComponents<Theme = unknown> {
  // ...
  GRouter<Name>?: {
    defaultProps?: GComponentsProps['GRouter<Name>'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouter<Name>'];
    variants?: GComponentsVariants['GRouter<Name>'];
  },
  // ...
}
```

`GComponentsProps`, `GComponentsVariants`, and `GComponentsOverrides` are derived types that update automatically when `GComponentsPropsList` and `GComponentNameToClassKey` change.

`GRouterOfferSummaryRps` (`FormFillRpsRating`) exists in the codebase but is not registered in `g-props.ts`. All new route components must be registered — do not omit this step.

---

## Step 8: Register the route file

**Existing `secured.$locale.views.$viewId.index.tsx` — adding a new `$viewId` dispatch:**

When the new route mounts as a secured view dispatched by `$viewId`, add a branch to `ChooseComponent` in `routes/secured.$locale.views.$viewId.index.tsx`:

```ts
import { GRouter<Name> } from '../g-router-<name>';

// In the Component() function, add the new route component import and dispatch branch.
// The Component() function and ChooseComponent() pattern in the real file:

function Component() {
  const { viewId, locale } = Route.useParams();
  const { topicId } = Route.useSearch();
  const { setLocale } = useLocale();

  React.useEffect(() => setLocale(locale), [locale]);

  return React.useMemo(() => (<ChooseComponent viewId={viewId as any} locale={locale} topicId={topicId} />), [viewId, locale, topicId]);
}

function ChooseComponent(props: { viewId: GUserOverviewMenuView, locale: string, topicId: string | undefined }) {
  const { viewId, locale = 'en', topicId } = props;

  // ... existing branches ...
  } else if (viewId === '<view-id-string>') {
    return <GRouter<Name> locale={locale} viewId={viewId} />;
  }
  return (<GErrorNotFound />);
}
```

`GUserOverviewMenuView` is a union type in `@dxs-ts/gamut-primitives`. The `<view-id-string>` value must match an existing member of that union. Do not add a new `GUserOverviewMenuView` value without extending that type in `gamut-primitives`.

**New route file:**

If the route mounts on a dedicated path rather than dispatched by `$viewId`, create a new file in `routes/` using TanStack Router dot-separator naming. The file must export a `Route` constant created via `createFileRoute`:

```tsx
import { createFileRoute } from '@tanstack/react-router';
import { GRouter<Name> } from '../g-router-<name>';

export const Route = createFileRoute('/public/$locale/<path>')({
  component: Component,
});

function Component() {
  const { locale } = Route.useParams();
  return <GRouter<Name> locale={locale} />;
}
```

After creating the file, run the TanStack Router CLI to regenerate `routeTree.gen.ts`. Do not edit `routeTree.gen.ts` manually.

---

## Checklist

- [ ] Create `g-router-<name>/` directory
- [ ] `useUtilityClasses.ts` (or `.tsx`) — `MUI_NAME`, `GRouter<Name>Classes`, `GRouter<Name>ClassKey`, `useUtilityClasses()`, `GRouter<Name>Root`
- [ ] Private sub-components (breadcrumbs, section title) added to `useUtilityClasses.tsx` if they do not warrant separate files
- [ ] `GRouter<Name>.tsx` — props interface (or imported from types file), `useThemeProps`, navigation handlers, `GShell` + `GAppBar` + `GLayout` + `GFooter` structure
- [ ] `g-router-<name>-types.ts` — only for complex routes with derived state; contains props interface, `OwnerState` interface, state hook
- [ ] Sub-component files (`GRouter<Name>Content.tsx`, etc.) — only for complex routes
- [ ] `g-router-<name>/index.ts` — barrel export: component, ClassKey, Classes; types file if present
- [ ] `export * from './g-router-<name>'` added to `gamut-routes/index.ts`
- [ ] `GRouter<Name>Props` and `GRouter<Name>ClassKey` imported in `g-props.ts`
- [ ] Entry added to `GComponentsPropsList` in `g-props.ts`
- [ ] Entry added to `GComponentNameToClassKey` in `g-props.ts`
- [ ] Entry added to `GComponents<Theme>` in `g-props.ts`
- [ ] Route file created or updated in `routes/` — either new `createFileRoute` file or new branch in `ChooseComponent`
- [ ] TanStack Router CLI run to regenerate `routeTree.gen.ts` (if new route file was created)
- [ ] Tested via `pnpm start-gamut` — renders at expected URL, navigation works, `useThemeProps` applies theme overrides

---

## Before finishing

Do not report `COMPLETE` until every applicable item is checked off. Use the Agent tool for each agent invocation — a self-review is never an acceptable substitute.

- [ ] Run `corepack pnpm build-gamut` and fix all TypeScript errors before proceeding.
- [ ] **ui-compliance** — Invoke the `ui-compliance` agent. Pass it the task spec and the full list of files created or modified.
  - [ ] If `FAIL`: invoke `ui-cleanup`, then invoke `ui-compliance` a second time.
  - [ ] If still `FAIL`: mark the task `BLOCKED` and stop.
- [ ] **doc-sync** — Check the Doc column in `digiexpress-parent/docs-parent/000_architecture.md` for the module you modified. If a doc exists, invoke the `doc-sync` agent with the module name, doc path, and list of files. If no doc exists, check this off and skip it.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `COMPLETE`:
- Files created and their paths
- Route type: `$viewId` dispatch or dedicated route file
- `$viewId` string registered in `ChooseComponent` (if applicable)
- Reference component used and why
- Whether `g-router-<name>-types.ts` was created and why
- `GComponentsPropsList` registration confirmed

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed
