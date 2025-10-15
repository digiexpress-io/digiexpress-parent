import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemService, mergeTagomiSearchParams, TagomiRouteSearchParams, toExplorerId } from './tagomi-nav-types';
import { useTagomiTabClose } from './useTagomiTabClose';



export function useTagomiNav(): { 
  activeItem: ExplorerItem | undefined;
  activeItemId: string | undefined;
  onNav: (newItem: ExplorerItem) => void;

  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;
  getService: () => ExplorerItemService;
  onTabCurrentClose: () => void;
  onTabClose: (tab: ExplorerItem) => void;
  explorer: ExplorerItem[];
} {
  const { onTabClose } = useTagomiTabClose();

  const navigate = useNavigate();
  const search = useSearch({ from: '/secured/$locale/assets/tagomi/' });
  const explorer: ExplorerItem[] = search.explorer;
  const explorerActive: string | undefined = search.explorerActive;

  const activeItem = explorer.find((explorer: ExplorerItem) => toExplorerId(explorer) === explorerActive) ?? explorer[explorer.length -1];

  const activeItemId = activeItem ? toExplorerId(activeItem): undefined;

  function onNav(input: ExplorerItem) {
    navigate({ 
      from: '/secured/$locale/assets/tagomi',
      to: '.',
      search: (prev: TagomiRouteSearchParams) => mergeTagomiSearchParams(input, prev)
    });
  }

  function findTab(newItem: ExplorerItem['type'], serviceId?: string): ExplorerItem | undefined {
    return explorer
      .filter((tab: ExplorerItem) => tab?.type === newItem)
      .find((tab: ExplorerItem) => serviceId ? (tab as any)['service'] === serviceId : true);
  }

  function getService(): ExplorerItemService {
    const service: ExplorerItemService | undefined = explorer.find(({ type }) => type === 'SERVICES') as ExplorerItemService | undefined;
    return service ?? { type: 'SERVICES', service: undefined, expanded: [] };
  }

  function onTabCurrentClose() {
    onTabClose(activeItem);
  }

  return { activeItem, activeItemId, explorer, onNav, onTabCurrentClose, findTab, getService, onTabClose }
}