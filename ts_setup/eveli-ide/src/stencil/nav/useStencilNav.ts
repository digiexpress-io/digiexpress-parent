import { useNavigate, useSearch } from '@tanstack/react-router';
import { ExplorerItem, ExplorerItemArticle, toExplorerId } from './stencil-nav-types';




function calculateNextSearch(newExplorerItem: ExplorerItem, prev: ExplorerItem[]): ExplorerItem[]  {
  const newItemId = toExplorerId(newExplorerItem);
  const explorer = [...prev.filter(explorer => toExplorerId(explorer) !== newItemId), newExplorerItem];
  return explorer;
}


export function useStencilNav(): { 
  activeItem: ExplorerItem | undefined;
  onNav: (newItem: ExplorerItem) => void;

  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;
  getArticle: () => ExplorerItemArticle;

  explorer: ExplorerItem[];
} {
  const navigate = useNavigate();
  const { explorer, explorerActive } = useSearch({ from: '/secured/$locale/assets/stencil/' });
  const activeItem = explorer.find(explorer => toExplorerId(explorer) === explorerActive) ?? explorer[explorer.length -1];

  function onNav(input: ExplorerItem) {
    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => ({
        ...prev,
        explorer: calculateNextSearch(input, prev.explorer),
      })
    });
  }

  function findTab(newItem: ExplorerItem['type'], articleId?: string): ExplorerItem | undefined {
    return explorer
      .filter(tab => tab?.type === newItem)
      .find(tab => articleId ? (tab as any)['article'] === articleId : true);
  }

  function getArticle(): ExplorerItemArticle {
    const article: ExplorerItemArticle | undefined = explorer.find(({ type }) => type === 'ARTICLES') as ExplorerItemArticle | undefined;
    return article ?? { type: 'ARTICLES', article: undefined, expanded: []};
  }

  return { activeItem, onNav, findTab, getArticle, explorer }
}