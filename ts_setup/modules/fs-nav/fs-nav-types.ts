import { Fs } from '@dxs-ts/fs-api';

export type FsTabDescriptor =
  | { type: 'edit'; id: string }
  | { type: 'create'; direntType: Fs.BodyType };

export interface FsRouteSearchParams {
  openTabs: FsTabDescriptor[];
  activeTab?: string;
}

export function toTabId(descriptor: FsTabDescriptor): string {
  if (descriptor.type === 'edit') {
    return `edit:${descriptor.id}`;
  }
  return `create:${descriptor.direntType}`;
}

export function parseFsSearchParams(search: Record<string, unknown>): FsRouteSearchParams {
  const openTabs = parseTabs(search['openTabs']);
  const rawActive = search['activeTab'];
  const activeTab = openTabs.find(t => toTabId(t) === rawActive)
    ? (rawActive as string)
    : openTabs.length > 0 ? toTabId(openTabs[0]) : undefined;
  return { openTabs, activeTab };
}

export function mergeFsSearchParams(
  descriptor: FsTabDescriptor,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const id = toTabId(descriptor);
  const exists = prev.openTabs.some(t => toTabId(t) === id);
  return {
    openTabs: exists ? prev.openTabs : [...prev.openTabs, descriptor],
    activeTab: id,
  };
}

export function closeFsTab(
  tabId: string,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const index = prev.openTabs.findIndex(t => toTabId(t) === tabId);
  if (index === -1) return prev;

  const newTabs = prev.openTabs.filter((_, i) => i !== index);
  const wasActive = prev.activeTab === tabId;

  let newActive: string | undefined;
  if (!wasActive) {
    newActive = prev.activeTab;
  } else if (newTabs.length === 0) {
    newActive = undefined;
  } else {
    const nextIndex = Math.max(0, index - 1);
    newActive = toTabId(newTabs[nextIndex]);
  }

  return { openTabs: newTabs, activeTab: newActive };
}

export function closeAllFsTabs(): FsRouteSearchParams {
  return { openTabs: [], activeTab: undefined };
}

export function closeTabsToTheRight(
  tabId: string,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const index = prev.openTabs.findIndex(t => toTabId(t) === tabId);
  if (index === -1) return prev;

  const newTabs = prev.openTabs.slice(0, index + 1);
  const activeStillOpen = newTabs.some(t => toTabId(t) === prev.activeTab);
  const newActive = activeStillOpen ? prev.activeTab : toTabId(newTabs[index]);

  return { openTabs: newTabs, activeTab: newActive };
}

function parseTabs(raw: unknown): FsTabDescriptor[] {
  if (!Array.isArray(raw) || raw.length === 0) return [];
  return raw.filter(isValidTabDescriptor);
}

function isValidTabDescriptor(item: unknown): item is FsTabDescriptor {
  if (typeof item !== 'object' || item === null) return false;
  const obj = item as Record<string, unknown>;
  if (obj['type'] === 'edit') return typeof obj['id'] === 'string';
  if (obj['type'] === 'create') return typeof obj['direntType'] === 'string';
  return false;
}
