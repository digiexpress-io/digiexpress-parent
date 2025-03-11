import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, toTab } from './wrench-nav-types';

import { OneTab } from '@/burger';

export function useWrenchTabClose() {
  const navigate = useNavigate();

  function onTabClose(tab: OneTab<any>, nextActive: OneTab<any> | undefined) {
    navigate({ 
      from: '/secured/$locale/assets/wrench', 
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