import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, toTab } from './stencil-nav-types';

import { OneTab } from '@/burger';

export function useStencilTabClose() {
  const navigate = useNavigate();

  function onTabClose(tab: OneTab<any>, nextActive: OneTab<any> | undefined) {
    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => {
        
        const explorer = [...prev.explorer].filter(t => toTab(t).id !== tab.id);
        const newItem: ExplorerItem | undefined = nextActive?.data;
  
        if(newItem) {
          const itemIndex = explorer.indexOf(newItem);
          if(itemIndex !== explorer.length - 1) {
            delete explorer[itemIndex];
            explorer.push(newItem);
          }
        }
  
        return { ...prev, explorer: explorer.filter(e => !!e) };
      }
    });
  }

  return { onTabClose }
}