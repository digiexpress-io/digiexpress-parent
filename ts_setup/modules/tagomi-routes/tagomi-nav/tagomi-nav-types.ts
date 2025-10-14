export interface ExplorerItemServiceTemplates {
  type: 'SERVICE_TEMPLATES';
  article: string;
  locale1: string;
  locale2?: string | undefined;
}

export interface ExplorerItemService {
  type: 'SERVICES';
  article?: string | undefined,
  expanded?: string[]
}

export type ExplorerItem = (
  ExplorerItemServiceTemplates |
  ExplorerItemService |

  { type: 'RESOURCES' } |
  { type: 'LOCALES' } |
  { type: 'TEMPLATES' } |
  { type: 'TAGS' }

)

export function toExplorerId(data: ExplorerItem): string {
  const id = JSON.stringify(Object.entries(data)
    .filter(([key]) => {
      if (data.type === 'SERVICES') {
        return key === 'type';
      }
      return key === 'type' || key === 'services'
    })
    .reduce((result, [, value]) => result + '/' + value, ''));
  return id;
}


export interface TagomiRouteSearchParams {
  explorer: ExplorerItem[];
  explorerActive?: string | undefined;
}

export function parseTagomiSearchParams(search: Record<string, unknown>): TagomiRouteSearchParams {
  const explorer = parseExplorerItems(search);
  const possiblyExplorerId = search.explorerActive;
  const explorerActive: ExplorerItem = explorer.find(item => toExplorerId(item) === possiblyExplorerId) ?? explorer[0];
  return { explorer, explorerActive: toExplorerId(explorerActive) }
}

export function mergeTagomiSearchParams(activeItem: ExplorerItem, prev: TagomiRouteSearchParams): TagomiRouteSearchParams {
  const newItemId = toExplorerId(activeItem);
  const isTabCreated: boolean = !!prev.explorer.find(tab => toExplorerId(tab) === newItemId)

  return {
    explorer: isTabCreated ?
      prev.explorer.map(item => toExplorerId(item) === newItemId ? activeItem : item) :     // -+
      [...prev.explorer, activeItem], // all open tabs + 1 new at the end
    explorerActive: toExplorerId(activeItem) // id of the active tab
  }
}

function parseExplorerItems(search: Record<string, unknown>): ExplorerItem[] {
  const explorerItems = search['explorer'];

  if (!Array.isArray(explorerItems)) {
    return [{ type: 'SERVICES' }];
  }
  if (explorerItems.length === 0) {
    return [{ type: 'SERVICES' }];
  }

  return explorerItems;
}