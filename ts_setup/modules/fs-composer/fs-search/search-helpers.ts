import { Fs } from "@dxs-ts/fs-api";

type AssetTypeFilter = { type: 'asset'; label: string; value: Fs.BodyType };
type LabelFilter = { type: 'label'; label: string; value: string };
type FilterData = AssetTypeFilter | LabelFilter;

export function filterTreeDirents(
  dirents: Fs.DirentBase[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => Fs.DirentBase | undefined,
  getExtension: (type: Fs.BodyType) => string | undefined,
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
    const extension = getExtension(dirent.type) ?? '';
    const displayName = dirent.name + extension;
    const nameMatches = displayName.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = direntEntry?.props?.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = typeFilters.length === 0 || typeFilters.some(f => f.value === dirent.type);
    const labelMatches = labelFilters.length === 0 || (direntEntry?.props?.labels ?? []).some(l => labelValues.includes(l.value));
    const childMatches = dirent.children ? filterTreeDirents(dirent.children, searchTerm, visibleFilters, getDirent, getExtension) : [];

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
  { type: 'asset', label: 'Articles', value: 'ARTICLE' },
  { type: 'asset', label: 'Dialobs', value: 'DIALOB_FORM' },
  { type: 'asset', label: 'Services', value: 'ARTICLE_WORKFLOW' },
  { type: 'asset', label: 'Pages', value: 'FOLDER' },
  { type: 'asset', label: 'Links', value: 'ARTICLE_LINK' },
  { type: 'asset', label: 'Phone Numbers', value: 'UNKNOWN' },
  { type: 'asset', label: 'Languages', value: 'LOCALE' },
  { type: 'asset', label: 'Flows', value: 'FLOW' },
  { type: 'asset', label: 'Printouts', value: 'PRINTOUT' },
  { type: 'asset', label: 'Images', value: 'UNKNOWN' },
];

export type { FilterData, AssetTypeFilter, LabelFilter };