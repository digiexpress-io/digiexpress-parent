import * as Yup from 'yup';
import { OneTab } from '@/burger';


export interface ExplorerItemArticlePages { 
  type: 'ARTICLE_PAGES';
  article: string;
  locale1: string; 
  locale2?: string | undefined;
}

export type ExplorerItem = (
  ExplorerItemArticlePages  |

  { type: 'ACTIVITIES' } | 
  { type: 'SERVICES' } | 
  { type: 'LINKS' } | 
  { type: 'LOCALES' } | 
  { type: 'MIGRATIONS' } | 
  { type: 'TEMPLATES' } | 
  { type: 'RELEASES' } |

  { type: 'ARTICLES', article: string | undefined } | 
  { type: 'ARTICLE_LINKS', article: string } |
  { type: 'ARTICLE_WORKFLOWS', article: string }
)

export function toTab(data: ExplorerItem): OneTab<any> {
  const id = JSON.stringify(Object.entries(data)
    .filter(([key]) => key === 'type' || key === 'article')
    .reduce((result, [key, value]) => result + '/' + value, ''));
  const label = data.type ? data.type.toLowerCase() : 'no type';
  return { id, label, data };
}


export interface StencilRouteSearchParams {
  explorer: ExplorerItem[];
  explorerActive?: string | undefined;
}
  
export function parseSearchParams(search: Record<string, unknown>): StencilRouteSearchParams {
  return {
    explorer: parseExplorerItems(search),
    explorerActive: search.explorerActive as any
  }
}

function parseExplorerItems(search: Record<string, unknown>): ExplorerItem[] {
  const explorerItems = search['explorer'];

  if(!Array.isArray(explorerItems)) {
    return [{ type: 'ARTICLES', article: undefined }];
  }
  return explorerItems;
}