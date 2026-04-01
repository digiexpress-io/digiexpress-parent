import { Fs } from "@dxs-ts/fs-api";

interface FilterData {
  label: string;
  type: Fs.Type;
}

export function filterTreeDirents(
  dirents: Fs.DirentBase[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => Fs.DirentAsset | undefined,
): Fs.DirentBase[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim();

  if (isSearchTermEmpty && isNoFiltersSelected) {
    return dirents;
  }

  const filtered: Fs.DirentBase[] = [];

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

export const allAvailableFilters: FilterData[] = [
  { label: 'Articles', type: 'article' },
  { label: 'Dialobs', type: 'dialob' },
  { label: 'Services', type: 'service' },
  { label: 'Pages', type: 'folder' },
  { label: 'Links', type: 'link' },
  { label: 'Phone Numbers', type: 'phone' },
  { label: 'Languages', type: 'language' },
  { label: 'Flows', type: 'flow' },
  { label: 'Printouts', type: 'printout' },
  { label: 'Images', type: 'image' }
];

export type { FilterData };