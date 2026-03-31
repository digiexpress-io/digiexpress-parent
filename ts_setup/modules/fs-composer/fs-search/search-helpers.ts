import { FsDirent } from "@dxs-ts/fs-api";

interface FilterData {
  label: string;
  type: FsDirent.Type;
}

export function filterTreeDirents(
  dirents: FsDirent.Dirent[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => FsDirent.Entry | undefined
): FsDirent.Dirent[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim();

  // If no search term and no filters, show everything
  if (isSearchTermEmpty && isNoFiltersSelected) {
    return dirents;
  }

  const filtered: FsDirent.Dirent[] = [];

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