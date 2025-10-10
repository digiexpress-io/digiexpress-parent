import { useNavigate } from '@tanstack/react-router';
import { ExplorerItem, TagomiRouteSearchParams, toExplorerId } from './tagomi-nav-types';


export function useTagomiTabChange() {
  const navigate = useNavigate();

  function onTabChange(nextActive: ExplorerItem | undefined) {
    if(!nextActive) {
      return;
    }

    navigate({ 
      from: '/secured/$locale/assets/tagomi', 
      to: '.',
      search: (prev: TagomiRouteSearchParams) => ({ ...prev, explorerActive: toExplorerId(nextActive) })
    });
  }

  return { onTabChange }
}