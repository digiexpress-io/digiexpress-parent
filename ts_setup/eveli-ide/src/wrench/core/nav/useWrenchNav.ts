import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemArticle, ExplorerItemArticlePages, toTab } from './wrench-nav-types';

import { useTabs } from '@/burger';

export type NavInput = (
  'ACTIVITIES'|
  'SERVICES'|
  'LINKS'|
  'LOCALES'|
  'MIGRATIONS'|
  'TEMPLATES' |
  'RELEASES' |

  ExplorerItemArticle |
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


export function useWrenchNav(): { 
  activeItem: ExplorerItem | undefined;
  onNav: (newItem: NavInput) => void;
  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;
  getArticle: () => ExplorerItemArticle
} {
  const navigate = useNavigate();
  const tabs = useTabs();
  
  const { explorer } = useSearch({ from: '/secured/$locale/assets/wrench/' });
  const activeItem = explorer.find(explorer => toTab(explorer).id === tabs.session.activeTab?.id);

  function onNav(input: NavInput) {
    const newItem = toExplorerItem(input);
    const newTab = toTab(newItem);
    tabs.handleTabAdd(newTab);

    const other: {} = newItem;

    navigate({ 
      from: '/secured/$locale/assets/wrench', 
      search: (prev) => ({
        ...prev, 
        ...other,
        explorer: calculateNextSearch(newItem, prev.explorer),
      })
    });
  }

  function findTab(newItem: ExplorerItem['type'], articleId?: string): ExplorerItem | undefined {
    return tabs.session.tabs
      .filter(tab => tab.data?.type === newItem)
      .find(tab => articleId ? tab.data?.article === articleId : true)?.data;
  }

  function getArticle(): ExplorerItemArticle {
    const article: ExplorerItemArticle | undefined = explorer.find(({ type }) => type === 'ARTICLES') as ExplorerItemArticle | undefined;
    return article ?? { type: 'ARTICLES', article: undefined, expanded: []};
  }

  return { activeItem, onNav, findTab, getArticle }
}