
import { TenantEntryDescriptor } from './types';

export function applySearchString(desc: TenantEntryDescriptor, searchString: string): boolean {
  const formName: boolean = desc.formName?.toLowerCase().indexOf(searchString) > -1;
  return desc.formName.toLowerCase().indexOf(searchString) > -1 || formName;
}

