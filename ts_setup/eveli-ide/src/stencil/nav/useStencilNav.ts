import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemArticlePages, toTab } from './stencil-nav-types';

import { OneTab, useTabs } from '@/burger';

export type NavInput = (
  'ACTIVITIES'|
  'ARTICLES'|
  'SERVICES'|
  'LINKS'|
  'LOCALES'|
  'MIGRATIONS'|
  'TEMPLATES' |
  'RELEASES' |

  ExplorerItemArticlePages |
  { type: 'ARTICLE_LINKS', article: string } |
  { type: 'ARTICLE_WORKFLOWS', article: string }
)


function toExplorerItem(input: NavInput): ExplorerItem {
  const data: ExplorerItem = ((input as any)['type'] ? input : { type: input }) as any;
  return data;
}


function calculateNextSearch(newExplorerItem: ExplorerItem, prev: ExplorerItem[]): ExplorerItem[]  {
  const newItemId = toTab(newExplorerItem).id;
  const explorer = [...prev.filter(explorer => toTab(explorer).id !== newItemId), newExplorerItem];
  return explorer;
}




export function useStencilNav(): { 
  activeItem: ExplorerItem | undefined;
  onNav: (newItem: NavInput) => void;
  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;
} {
  const navigate = useNavigate();
  const tabs = useTabs();
  
  const { explorer } = useSearch({ from: '/secured/$locale/assets/stencil/' });
  const activeItem = explorer.find(explorer => toTab(explorer).id === tabs.session.activeTab?.id);

  function onNav(input: NavInput) {
    const newItem = toExplorerItem(input);
    const newTab = toTab(newItem);
    tabs.handleTabAdd(newTab);
    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => ({ ...prev, explorer: calculateNextSearch(newItem, prev.explorer) })
    });
  }

  function findTab(newItem: ExplorerItem['type'], articleId?: string): ExplorerItem | undefined {
    return tabs.session.tabs
      .filter(tab => tab.data?.type === newItem)
      .find(tab => articleId ? tab.data?.article === articleId : true)?.data;
  }
  return { activeItem, onNav, findTab }
}