import { Fs } from "@dxs-ts/fs-api";
import { createWidget, allWidgets } from "../fs-factory";

type AssetTypeFilter = { type: 'asset'; value: Fs.BodyType };
type LabelFilter = { type: 'label'; label: string; value: string };
type FilterData = AssetTypeFilter | LabelFilter;

export function filterTreeDirents(
  dirents: Fs.DirentBase[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => Fs.DirentBase | undefined,
): Fs.DirentBase[] {

  const typeFilters = visibleFilters.filter((f): f is AssetTypeFilter => f.type === 'asset');
  const labelFilters = visibleFilters.filter((f): f is LabelFilter => f.type === 'label');
  const isSearchTermEmpty = !searchTerm.trim();

  if (isSearchTermEmpty && visibleFilters.length === 0) {
    return dirents;
  }

  const labelValues = labelFilters.map(f => f.value);
  const filtered: Fs.DirentBase[] = [];

  for (const dirent of dirents) {
    const direntEntry = getDirent(dirent.id);
    const widget = createWidget(direntEntry!);

    const displayName = dirent.name + (widget.meta.extension ?? '');
    const nameMatches = displayName.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = direntEntry?.props?.assetDescription?.toLowerCase().includes(searchTerm.toLowerCase());
    const direntType = dirent.type === 'DIALOB_FORM_META' ? 'DIALOB_FORM' : dirent.type;
    const typeMatches = typeFilters.length === 0 || typeFilters.some(f => f.value === direntType);
    const labelMatches = labelFilters.length === 0 || (direntEntry?.props?.labels ?? []).some(l => labelValues.includes(l.key));
    const childMatches = dirent.children?.length
      ? filterTreeDirents(dirent.children, searchTerm, visibleFilters, getDirent)
      : [];

    const selfMatches = (isSearchTermEmpty || nameMatches || descriptionMatches) && typeMatches && labelMatches;

    if (selfMatches) {
      filtered.push({ ...dirent, children: childMatches });
    } else {
      filtered.push(...childMatches);
    }
  }

  return filtered;
}
export const allAvailableTypeFilters: AssetTypeFilter[] = allWidgets.map(w => ({ type: 'asset' as const, value: w.meta.type }));

export type { FilterData, AssetTypeFilter, LabelFilter };