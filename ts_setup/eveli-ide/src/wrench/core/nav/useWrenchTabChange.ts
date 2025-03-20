import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, toExplorerId, WrenchRouteSearchParams } from './wrench-nav-types';



export function useWrenchTabChange() {
  const navigate = useNavigate();

  function onTabChange(nextActive: ExplorerItem | undefined) {
    if(!nextActive) {
      return;
    }
    navigate({ 
      from: '/secured/$locale/assets/wrench', 
      search: (prev: WrenchRouteSearchParams) => ({ ...prev, explorerActive: toExplorerId(nextActive) })
    });
  }

  return { onTabChange }
}