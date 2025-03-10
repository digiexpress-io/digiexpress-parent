import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, toTab } from './stencil-nav-types';

import { OneTab } from '@/burger';

export function useStencilTabChange() {
  const navigate = useNavigate();

  function onTabChange(tab: OneTab<any>, nextActive: OneTab<any> | undefined) {
    const explorer = nextActive; 

    if(!explorer) {
      return;
    }

    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => ({ ...prev, explorerActive: explorer.id })
    });
  }

  return { onTabChange }
}