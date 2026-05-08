import { useFsNavContext } from './FsNavProvider';
import { useFsRouteNav } from './useFsRouteNav';

export function useFsNav() {
  const { isDarkMode, setIsDarkMode, expandedIds, isExpanded, setExpanded, setExpandedBatch, collapseAll } = useFsNavContext();
  const routeNav = useFsRouteNav();

  return {
    isDarkMode,
    setIsDarkMode,
    expandedIds,
    isExpanded,
    setExpanded,
    setExpandedBatch,
    collapseAll,
    ...routeNav,
  };
}
