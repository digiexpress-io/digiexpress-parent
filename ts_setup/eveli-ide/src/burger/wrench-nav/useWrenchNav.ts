import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemDecisions, ExplorerItemFlows, ExplorerItemServices, toExplorerId, WrenchRouteSearchParams } from './wrench-nav-types';
import { useWrenchTabClose } from './useWrenchTabClose';



function toExplorerItem(input: ExplorerItem): ExplorerItem {
  const data: ExplorerItem = ((input as any)['type'] ? input : { type: input }) as any;
  return data;
}


function calculateNextSearch(newExplorerItem: ExplorerItem, prev: ExplorerItem[]): ExplorerItem[]  {
  const newItemId = toExplorerId(newExplorerItem);
  const explorer = [...prev.filter(explorer => toExplorerId(explorer) !== newItemId), newExplorerItem];
  return explorer;
}


export function useWrenchNav(): { 
  activeItem: ExplorerItem | undefined;
  explorer: ExplorerItem[];
  onNav: (newItem: ExplorerItem) => void;
  onNavReset: (newItem: ExplorerItem[]) => void;
  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;

  onTabClose: (tab: ExplorerItem) => void;
  onTabCurrentClose: () => void;
  getFlows: () => ExplorerItemFlows;
  getDecisions: () => ExplorerItemDecisions;
  getServices: () => ExplorerItemServices;

} {

  const { onTabClose } = useWrenchTabClose();
  
  const navigate = useNavigate();

  const search = useSearch({ from: '/secured/$locale/assets/wrench/' });
  const explorer: ExplorerItem[] = search.explorer;
  const explorerActive: string | undefined = search.explorerActive;



  const activeItem = explorer.find(explorer => toExplorerId(explorer) === explorerActive) ?? explorer[explorer.length -1];

  function onNav(input: ExplorerItem) {
    const newItem = toExplorerItem(input);
    const other: {} = newItem;

    navigate({ 
      from: '/secured/$locale/assets/wrench', 
      search: (prev: WrenchRouteSearchParams) => ({
        ...prev, 
        ...other,
        explorer: calculateNextSearch(newItem, prev.explorer),
      })
    });
  }

  function findTab(newItem: ExplorerItem['type'], articleId?: string): ExplorerItem | undefined {
    return explorer
      .filter(tab => toExplorerId(tab) === newItem)
      .find(tab => articleId ? (tab as any).id === articleId : true);
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

  function onTabCurrentClose() {
    if(activeItem) {
      onTabClose(activeItem);
    }
  }

  function onNavReset(newItem: ExplorerItem[]) {
    const last = newItem[newItem.length - 1];

    navigate({ 
      from: '/secured/$locale/assets/wrench', 
      search: (prev: WrenchRouteSearchParams) => ({
        ...prev,
        explorer: [...newItem],
        explorerActive: last ? toExplorerId(last) : undefined
      })
    });
  }

  return { activeItem, explorer, onNav, findTab, getFlows, getDecisions, getServices, onTabCurrentClose, onTabClose, onNavReset }
}