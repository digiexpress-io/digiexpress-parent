import { FsNode } from "@dxs-ts/fs-api";
import { FsNodeType } from '../fs-theme';

interface FilterData {
  label: string;
  type: FsNodeType;
}

export function filterTreeNodes(
  nodes: FsNode[],
  searchTerm: string,
  visibleFilters: FilterData[]
): FsNode[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim() || searchTerm.trim().length < 3;

  // If no search term and no filters, show everything
  if (isSearchTermEmpty && isNoFiltersSelected) {
    return nodes;
  }

  const filtered: FsNode[] = [];

  for (const node of nodes) {
    const nameMatches = node.name.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = node.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = isNoFiltersSelected || visibleTypes.includes(node.type);
    const childMatches = node.children ? filterTreeNodes(node.children, searchTerm, visibleFilters) : [];

    const showBySearch = isSearchTermEmpty || nameMatches || descriptionMatches;

    if ((showBySearch && typeMatches) || childMatches.length > 0) {
      filtered.push({
        ...node,
        expanded: childMatches.length > 0 ? true : node.expanded,
        children: childMatches.length > 0 ? childMatches : node.children
      });
    }
  }

  return filtered;
}

export type { FilterData };