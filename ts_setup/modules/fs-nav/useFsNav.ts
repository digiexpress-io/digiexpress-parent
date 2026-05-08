import { useFsNavContext } from './FsNavProvider';
import { useFsRouteNav } from './useFsRouteNav';

export function useFsNav() {
  const { expandedIds, isExpanded, setExpanded, setExpandedBatch, collapseAll } = useFsNavContext();
  const routeNav = useFsRouteNav();

  return {
    expandedIds,
    isExpanded,
    setExpanded,
    setExpandedBatch,
    collapseAll,
    ...routeNav,
  };
}
