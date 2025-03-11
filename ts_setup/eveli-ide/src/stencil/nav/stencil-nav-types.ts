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
  { type: 'MIGRATIONS' } |
  { type: 'TEMPLATES' } |
  { type: 'RELEASES' } |


  { type: 'ARTICLE_LINKS', article: string } |
  { type: 'ARTICLE_WORKFLOWS', article: string }
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