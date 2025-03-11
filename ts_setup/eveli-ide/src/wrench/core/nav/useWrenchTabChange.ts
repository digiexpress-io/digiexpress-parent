import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, toTab } from './wrench-nav-types';

import { OneTab } from '@/burger';

export function useWrenchTabChange() {
  const navigate = useNavigate();

  function onTabChange(tab: OneTab<any>, nextActive: OneTab<any> | undefined) {
    const explorer = nextActive; 

    if(!explorer) {
      return;
    }

    navigate({ 
      from: '/secured/$locale/assets/wrench', 
      search: (prev) => ({ ...prev, explorerActive: explorer.id })
    });
  }

  return { onTabChange }
}