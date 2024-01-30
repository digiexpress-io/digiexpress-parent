import { Palette, _nobody_ } from './constants';
import { TaskDescriptor } from './types';

import { TaskGroupsAndFiltersImpl } from './TaskGroupsAndFiltersImpl';


export function toGroupsAndFilters(data: readonly TaskDescriptor[]): TaskGroupsAndFiltersImpl {
  const next = [...data];
  return new TaskGroupsAndFiltersImpl({
    data: next,
    filtered: next,
    filterBy: [],
    groupBy: 'status',
    groups: [],
    searchString: undefined,
  });
}


export const StatusPalette = Palette.status;
export const PriorityPalette = Palette.priority;
export const TeamGroupPalette = Palette.teamGroupType;
export const AssigneePalette = Palette.assigneeGroupType;
export const Nobody = _nobody_;



export * from './ImmutableTaskSearch';
export * from './ImmutableTaskDescriptor';
export * from './TasksContext';
export * from './TaskEditContext';
export * from './types';
export { Palette }

