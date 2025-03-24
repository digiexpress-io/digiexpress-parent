import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, StencilRouteSearchParams, toExplorerId } from './stencil-nav-types';


export function useStencilTabChange() {
  const navigate = useNavigate();

  function onTabChange(nextActive: ExplorerItem | undefined) {
    if(!nextActive) {
      return;
    }

    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev: StencilRouteSearchParams) => ({ ...prev, explorerActive: toExplorerId(nextActive) })
    });
  }

  return { onTabChange }
}