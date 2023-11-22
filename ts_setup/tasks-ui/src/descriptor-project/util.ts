
import { ProjectDescriptor, FilterBy } from './types';
import { Palette } from './constants'


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