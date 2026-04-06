import { Fs } from "@dxs-ts/fs-api";

type AssetTypeFilter = { type: 'asset'; label: string; value: Fs.Type };
type LabelFilter = { type: 'label'; label: string; value: string };
type FilterData = AssetTypeFilter | LabelFilter;

export function filterTreeDirents(
  dirents: Fs.DirentBase[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => Fs.DirentAsset | undefined,
): Fs.DirentBase[] {
  const typeFilters = visibleFilters.filter((f): f is AssetTypeFilter => f.type === 'asset');
  const labelFilters = visibleFilters.filter((f): f is LabelFilter => f.type === 'label');
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim();

  if (isSearchTermEmpty && isNoFiltersSelected) {
    return dirents;
  }

  const labelValues = labelFilters.map(f => f.value);
  const filtered: Fs.DirentBase[] = [];

  for (const dirent of dirents) {
    const direntEntry = getDirent(dirent.id);
    const nameMatches = dirent.name.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = direntEntry?.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = typeFilters.length === 0 || typeFilters.some(f => f.value === dirent.type);
    const labelMatches = labelFilters.length === 0 || (direntEntry?.labels ?? []).some(l => labelValues.includes(l.value));
    const childMatches = dirent.children ? filterTreeDirents(dirent.children, searchTerm, visibleFilters, getDirent) : [];

    const showBySearch = isSearchTermEmpty || nameMatches || descriptionMatches;

    if ((showBySearch && typeMatches && labelMatches) || childMatches.length > 0) {
      filtered.push({
        ...dirent,
        children: childMatches
      });
    }
  }

  return filtered;
}

export const allAvailableTypeFilters: AssetTypeFilter[] = [
  { type: 'asset', label: 'Articles', value: 'article' },
  { type: 'asset', label: 'Dialobs', value: 'dialob' },
  { type: 'asset', label: 'Services', value: 'service' },
  { type: 'asset', label: 'Pages', value: 'folder' },
  { type: 'asset', label: 'Links', value: 'link' },
  { type: 'asset', label: 'Phone Numbers', value: 'phone' },
  { type: 'asset', label: 'Languages', value: 'language' },
  { type: 'asset', label: 'Flows', value: 'flow' },
  { type: 'asset', label: 'Printouts', value: 'printout' },
  { type: 'asset', label: 'Images', value: 'image' },
];

export type { FilterData, AssetTypeFilter, LabelFilter };