export interface ExplorerItemEntity {
  type: 'ENTITY_EDITOR';
  id: string
}

export interface ExplorerItemFlows {
  type: 'FLOWS';
  id?: string | undefined;
  expanded?: string[];
}

export interface ExplorerItemDecisions {
  type: 'DECISIONS';
  id?: string | undefined;
  expanded?: string[];
}

export interface ExplorerItemServices {
  type: 'SERVICES',
  id?: string | undefined;
  expanded?: string[];
}

export type ExplorerItem = (
  ExplorerItemEntity |
  ExplorerItemFlows |
  ExplorerItemDecisions |
  ExplorerItemServices |

  { type: 'DEBUG' } |
  { type: 'ACTIVITIES' } |
  { type: 'COMPARE' } |
  { type: 'MIGRATIONS' }
)

export function toExplorerId(data: ExplorerItem): string {
  const id = JSON.stringify(Object.entries(data)
    .filter(([key]) => {

      if (data.type === 'FLOWS' || data.type === 'SERVICES' || data.type === 'DECISIONS') {
        return key === 'type';
      }

      return key === 'type' || key === 'id'
    })
    .reduce((result, [key, value]) => result + '/' + value, ''));
  return id;
}


export interface WrenchRouteSearchParams {
  explorer: ExplorerItem[];
  explorerActive?: string | undefined;
}

export function parseWrenchSearchParams(search: Record<string, unknown>): WrenchRouteSearchParams {
  const explorer = parseExplorerItems(search);
  const possiblyExplorerId = search.explorerActive;
  const explorerActive: ExplorerItem = explorer.find(item => toExplorerId(item) === possiblyExplorerId) ?? explorer[0];
  return { explorer, explorerActive: toExplorerId(explorerActive) }
}


export function mergeWrenchSearchParams(activeItem: ExplorerItem, prev: WrenchRouteSearchParams): WrenchRouteSearchParams {
  const newItemId = toExplorerId(activeItem);
  const isTabCreated: boolean = !!prev.explorer.find(tab => toExplorerId(tab) === newItemId)

  return {
    explorer: isTabCreated ?
      prev.explorer.map(item => toExplorerId(item) === newItemId ? activeItem : item) :                 // -+
      [...prev.explorer, activeItem], // all open tabs + 1 new at the end
    explorerActive: toExplorerId(activeItem) // id of the active tab
  }
}

function parseExplorerItems(search: Record<string, unknown>): ExplorerItem[] {
  const explorerItems = search['explorer'];
  return explorerItems as ExplorerItem[];
}