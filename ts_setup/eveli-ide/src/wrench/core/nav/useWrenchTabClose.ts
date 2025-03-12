import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, toExplorerId } from './wrench-nav-types';


export function useWrenchTabClose() {
  const navigate = useNavigate();

  function onTabClose(tab: ExplorerItem) {
    navigate({ 
      from: '/secured/$locale/assets/wrench', 
      search: (prev) => {
        
        const targetId = toExplorerId(tab);
        const explorer = [...prev.explorer].filter(t => toExplorerId(t) !== targetId);
        const newItem: ExplorerItem | undefined = explorer[explorer.length-1];
  
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