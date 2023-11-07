
import { TenantEntryDescriptor } from './descriptor-types';
import { Palette } from './descriptor-constants';


export function applySearchString(desc: TenantEntryDescriptor, searchString: string): boolean {
  const formName: boolean = desc.formName?.toLowerCase().indexOf(searchString) > -1;
  return desc.formName.toLowerCase().indexOf(searchString) > -1 || formName;
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