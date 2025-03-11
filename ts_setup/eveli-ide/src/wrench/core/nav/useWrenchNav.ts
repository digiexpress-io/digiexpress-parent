import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemDecisions, ExplorerItemFlows, ExplorerItemServices, toTab } from './wrench-nav-types';

import { useTabs } from '@/burger';



function toExplorerItem(input: ExplorerItem): ExplorerItem {
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
  onNav: (newItem: ExplorerItem) => void;
  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;


  getFlows: () => ExplorerItemFlows;
  getDecisions: () => ExplorerItemDecisions;
  getServices: () => ExplorerItemServices;

} {
  const navigate = useNavigate();
  const tabs = useTabs();
  
  const { explorer } = useSearch({ from: '/secured/$locale/assets/wrench/' });
  const activeItem = explorer.find(explorer => toTab(explorer).id === tabs.session.activeTab?.id);

  function onNav(input: ExplorerItem) {
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

  function getFlows(): ExplorerItemFlows {
    const article: ExplorerItemFlows | undefined = explorer.find(({ type }) => type === 'FLOWS') as ExplorerItemFlows | undefined;
    return article ?? { type: 'FLOWS', id: undefined, expanded: [] };
  }

  function getDecisions(): ExplorerItemDecisions {
    const article: ExplorerItemDecisions | undefined = explorer.find(({ type }) => type === 'DECISIONS') as ExplorerItemDecisions | undefined;
    return article ?? { type: 'DECISIONS', id: undefined, expanded: [] };
  }

  function getServices(): ExplorerItemServices {
    const article: ExplorerItemServices | undefined = explorer.find(({ type }) => type === 'SERVICES') as ExplorerItemServices | undefined;
    return article ?? { type: 'SERVICES', id: undefined, expanded: [] };
  }

  return { activeItem, onNav, findTab, getFlows, getDecisions, getServices }
}