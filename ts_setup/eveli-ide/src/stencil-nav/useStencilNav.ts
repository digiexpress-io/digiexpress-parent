import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemArticle, mergeStencilSearchParams, StencilRouteSearchParams, toExplorerId } from './stencil-nav-types';
import { useStencilTabClose } from './useStencilTabClose';



export function useStencilNav(): { 
  activeItem: ExplorerItem | undefined;
  activeItemId: string | undefined;
  onNav: (newItem: ExplorerItem) => void;

  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;
  getArticle: () => ExplorerItemArticle;
  onTabCurrentClose: () => void;
  onTabClose: (tab: ExplorerItem) => void;
  explorer: ExplorerItem[];
} {
  const { onTabClose } = useStencilTabClose();

  const navigate = useNavigate();
  const search = useSearch({ from: '/secured/$locale/assets/stencil/' });
  const explorer: ExplorerItem[] = search.explorer;
  const explorerActive: string | undefined = search.explorerActive;

  const activeItem = explorer.find((explorer: ExplorerItem) => toExplorerId(explorer) === explorerActive) ?? explorer[explorer.length -1];

  const activeItemId = activeItem ? toExplorerId(activeItem): undefined;

  function onNav(input: ExplorerItem) {
    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev: StencilRouteSearchParams) => mergeStencilSearchParams(input, prev)
    });
  }

  function findTab(newItem: ExplorerItem['type'], articleId?: string): ExplorerItem | undefined {
    return explorer
      .filter((tab: ExplorerItem) => tab?.type === newItem)
      .find((tab: ExplorerItem) => articleId ? (tab as any)['article'] === articleId : true);
  }

  function getArticle(): ExplorerItemArticle {
    const article: ExplorerItemArticle | undefined = explorer.find(({ type }) => type === 'ARTICLES') as ExplorerItemArticle | undefined;
    return article ?? { type: 'ARTICLES', article: undefined, expanded: []};
  }

  function onTabCurrentClose() {
    onTabClose(activeItem);
  }

  return { activeItem, activeItemId, explorer, onNav, onTabCurrentClose, findTab, getArticle, onTabClose }
}