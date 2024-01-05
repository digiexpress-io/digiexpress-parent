
import { ProjectDescriptor, FilterBy } from './types';

export function applyDescFilters(desc: ProjectDescriptor, filters: FilterBy[]): boolean {
  for (const filter of filters) {
    if (filter.disabled) {
      continue;
    }
  }

  return true;
}

export function applySearchString(desc: ProjectDescriptor, searchString: string): boolean {
  const description: boolean = desc.name?.toLowerCase().indexOf(searchString) > -1;
  return desc.id.toLowerCase().indexOf(searchString) > -1 || description;
}
