import { Fs } from '@dxs-ts/fs-api';

export interface FsEditTab {
  type: 'edit';
  dirent: Fs.DirentBase;
}

export interface FsCreateTab {
  type: 'create';
  direntType: Fs.BodyType;
  parentFolder: Fs.DirentBase | undefined;
}

export type FsTab = FsEditTab | FsCreateTab;


export type FsTabDescriptor =
  | { type: 'edit'; id: string }
  | { type: 'create'; direntType: Fs.BodyType; instanceId: string };

export interface FsRouteSearchParams {
  openTabs: FsTabDescriptor[];
  activeTab?: string;
  expandedIds?: string[];
}

export function toTabId(descriptor: FsTabDescriptor): string {
  if (descriptor.type === 'edit') {
    return `edit:${descriptor.id}`;
  }
  return `create:${descriptor.direntType}:${descriptor.instanceId}`;
}

export function parseFsSearchParams(search: Record<string, unknown>): FsRouteSearchParams {
  const openTabs = parseTabs(search['openTabs']);
  const rawActive = search['activeTab'];
  const expandedIds = parseExpandedIds(search['expandedIds']);
  const activeTab = openTabs.find(t => toTabId(t) === rawActive) ? (rawActive as string) : openTabs.length > 0 ? toTabId(openTabs[0]) : undefined;
  return { openTabs, activeTab, expandedIds };
}

export function mergeFsSearchParams(
  descriptor: FsTabDescriptor,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const id = toTabId(descriptor);
  const exists = prev.openTabs.some(t => toTabId(t) === id);
  return {
    ...prev,
    openTabs: exists ? prev.openTabs : [...prev.openTabs, descriptor],
    activeTab: id,
  };
}

export function closeFsTab(
  tabId: string,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const index = prev.openTabs.findIndex(t => toTabId(t) === tabId);
  if (index === -1) {
    return prev;
  }

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

  return { ...prev, openTabs: newTabs, activeTab: newActive };
}

export function closeAllFsTabs(): FsRouteSearchParams {
  return { openTabs: [], activeTab: undefined, expandedIds: [] };
}

export function closeTabsToTheRight(
  tabId: string,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const index = prev.openTabs.findIndex(t => toTabId(t) === tabId);
  if (index === -1) {
    return prev;
  }

  const newTabs = prev.openTabs.slice(0, index + 1);
  const activeStillOpen = newTabs.some(t => toTabId(t) === prev.activeTab);
  const newActive = activeStillOpen ? prev.activeTab : toTabId(newTabs[index]);

  return { ...prev, openTabs: newTabs, activeTab: newActive };
}

export function closeOtherFsTabs(
  tabId: string,
  prev: FsRouteSearchParams
): FsRouteSearchParams {
  const tab = prev.openTabs.find(t => toTabId(t) === tabId);
  if (!tab) {
    return prev;
  }

  return { ...prev, openTabs: [tab], activeTab: tabId };
}

function parseExpandedIds(raw: unknown): string[] {
  if (!Array.isArray(raw)) {
    return [];
  }
  return raw.filter((item): item is string => typeof item === 'string');
}

function parseTabs(raw: unknown): FsTabDescriptor[] {
  if (!Array.isArray(raw) || raw.length === 0) {
    return [];
  }
  return raw.filter(isValidTabDescriptor);
}

function isValidTabDescriptor(item: unknown): item is FsTabDescriptor {
  if (typeof item !== 'object' || item === null) {
    return false;
  }
  const obj = item as Record<string, unknown>;
  if (obj['type'] === 'edit') {
    return typeof obj['id'] === 'string';
  }
  if (obj['type'] === 'create') {
    return typeof obj['direntType'] === 'string' && typeof obj['instanceId'] === 'string';
  }
  return false;
}
