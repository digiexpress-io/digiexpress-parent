
import { ProjectDescriptor, FilterBy } from './descriptor-types';
import { _nobody_, Palette } from './descriptor-constants'


export function applyDescFilters(desc: ProjectDescriptor, filters: FilterBy[]): boolean {
  for (const filter of filters) {
    if (filter.disabled) {
      continue;
    }
    if (!applyDescFilter(desc, filter)) {
      return false;
    }
  }

  return true;
}

export function applySearchString(desc: ProjectDescriptor, searchString: string): boolean {
  const description: boolean = desc.description?.toLowerCase().indexOf(searchString) > -1;
  return desc.title.toLowerCase().indexOf(searchString) > -1 || description;
}

export function applyDescFilter(desc: ProjectDescriptor, filter: FilterBy): boolean {
  switch (filter.type) {
    case 'FilterByUsers': {
      for (const owner of filter.users) {
        if (desc.users.length === 0 && owner === _nobody_) {
          continue;
        }
        if (!desc.users.includes(owner)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByRepoType': {
      return filter.repoType.includes(desc.repoType);
    }
  }
  // @ts-ignore
  throw new Error("unknow filter" + filter)
}

export function withColors<T>(input: T[]): { color: string, value: T }[] {
  const result: { color: string, value: T }[] = [];
  const colors = Object.values(Palette.colors);
  let index = 0;
  for (const value of input) {
    result.push({ value, color: colors[index] })
    if (colors.length - 1 === index) {
      index = 0;
    } else {
      index++;
    }
  }

  return result;
}