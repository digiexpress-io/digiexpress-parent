import { FsDirent, FsDirentEntry, FsDirentType } from "@dxs-ts/fs-api";

interface FilterData {
  label: string;
  type: FsDirentType;
}

export function filterTreeDirents(
  dirents: FsDirent[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => FsDirentEntry | undefined
): FsDirent[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim();

  // If no search term and no filters, show everything
  if (isSearchTermEmpty && isNoFiltersSelected) {
    return dirents;
  }

  const filtered: FsDirent[] = [];

  for (const dirent of dirents) {
    const direntEntry = getDirent(dirent.id);
    const nameMatches = dirent.name.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = direntEntry?.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = isNoFiltersSelected || visibleTypes.includes(dirent.type);
    const childMatches = dirent.children ? filterTreeDirents(dirent.children, searchTerm, visibleFilters, getDirent) : [];

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