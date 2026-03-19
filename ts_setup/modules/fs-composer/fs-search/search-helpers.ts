import { FsDirent, FsDirentType, FsDirentProps } from "@dxs-ts/fs-api";

interface FilterData {
  label: string;
  type: FsDirentType;
}

export function filterTreeDirents(
  dirents: FsDirent[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirentProps: (id: string) => FsDirentProps
): FsDirent[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim() || searchTerm.trim().length < 3;

  // If no search term and no filters, show everything
  if (isSearchTermEmpty && isNoFiltersSelected) {
    return dirents;
  }

  const filtered: FsDirent[] = [];

  for (const dirent of dirents) {
    const direntProps = getDirentProps(dirent.id);
    const nameMatches = dirent.name.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = direntProps.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = isNoFiltersSelected || visibleTypes.includes(dirent.type);
    const childMatches = dirent.children ? filterTreeDirents(dirent.children, searchTerm, visibleFilters, getDirentProps) : [];

    const showBySearch = isSearchTermEmpty || nameMatches || descriptionMatches;

    if ((showBySearch && typeMatches) || childMatches.length > 0) {
      filtered.push({
        ...dirent,
        children: childMatches
      });
    }
  }

  return filtered;
}

export type { FilterData };