export interface ExplorerItemArticlePages {
  type: 'ARTICLE_PAGES';
  article: string;
  locale1: string;
  locale2?: string | undefined;
}

export interface ExplorerItemArticle {
  type: 'ARTICLES';
  article?: string | undefined,
  expanded?: string[]
}

export type ExplorerItem = (
  ExplorerItemArticlePages |
  ExplorerItemArticle |

  { type: 'ACTIVITIES' } |
  { type: 'SERVICES' } |
  { type: 'LINKS' } |
  { type: 'LOCALES' } |
  { type: 'ASSISTANCE' } |
  { type: 'TEMPLATES' } |

  { type: 'ARTICLE_LINKS', article: string } |
  { type: 'ARTICLE_WORKFLOWS', article: string } |
  { type: 'SEARCH' }
)

export function toExplorerId(data: ExplorerItem): string {
  const id = JSON.stringify(Object.entries(data)
    .filter(([key]) => {
      if (data.type === 'ARTICLES') {
        return key === 'type';
      }

      return key === 'type' || key === 'article'
    })
    .reduce((result, [, value]) => result + '/' + value, ''));
  return id;
}


export interface StencilRouteSearchParams {
  explorer: ExplorerItem[];
  explorerActive?: string | undefined;
}

export function parseStencilSearchParams(search: Record<string, unknown>): StencilRouteSearchParams {
  const explorer = parseExplorerItems(search);
  const possiblyExplorerId = search.explorerActive;
  const explorerActive: ExplorerItem = explorer.find(item => toExplorerId(item) === possiblyExplorerId) ?? explorer[0];
  return { explorer, explorerActive: toExplorerId(explorerActive) }
}

export function mergeStencilSearchParams(activeItem: ExplorerItem, prev: StencilRouteSearchParams): StencilRouteSearchParams {
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
    return [{ type: 'ASSISTANCE' }];
  }
  if (explorerItems.length === 0) {
    return [{ type: 'ASSISTANCE' }];
  }

  return explorerItems;
}