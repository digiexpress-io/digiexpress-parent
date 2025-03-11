import { OneTab } from '@/burger';



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

export type ExplorerItem = (
  ExplorerItemEntity |
  ExplorerItemFlows |
  ExplorerItemDecisions |

  { type: 'SERVICES' } |
  { type: 'DEBUG' } |
  { type: 'ACTIVITIES' } |
  { type: 'RELEASES' } |
  { type: 'COMPARE' } |
  { type: 'MIGRATIONS' }
)

export function toTab(data: ExplorerItem): OneTab<any> {
  const id = JSON.stringify(Object.entries(data)
    .filter(([key]) => {

      if (data.type === 'FLOWS' || data.type === 'SERVICES' || data.type === 'DECISIONS') {
        return key === 'type';
      }

      return key === 'type' || key === 'id'
    })
    .reduce((result, [key, value]) => result + '/' + value, ''));
  const label = data.type ? data.type.toLowerCase() : 'no type';
  return { id, label, data };
}


export interface WrenchRouteSearchParams {
  explorer: ExplorerItem[];
  explorerActive?: string | undefined;
}

export function parseWrenchSearchParams(search: Record<string, unknown>): WrenchRouteSearchParams {
  return {
    explorer: parseExplorerItems(search),
    explorerActive: search.explorerActive as any
  }
}

function parseExplorerItems(search: Record<string, unknown>): ExplorerItem[] {
  const explorerItems = search['explorer'];

  if (!Array.isArray(explorerItems)) {
    return [{ type: 'ACTIVITIES' }];
  }
  if (explorerItems.length === 0) {
    return [{ type: 'ACTIVITIES' }];
  }

  return explorerItems;
}