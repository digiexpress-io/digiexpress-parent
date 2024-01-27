
import { AssigneePalette, TaskDescriptor, AssigneeGroupType } from 'descriptor-task';
import { getInstance as createGroups, ImmutableCollection, Group, GroupingContextType } from 'descriptor-grouping';
import { getInstance as createTabs, SingleTabInit} from 'descriptor-tabbing';
import Table from 'table';


export type TaskPagination = Table.TablePagination<TaskDescriptor>;
export type TabTypes = 'recentActivities' | AssigneeGroupType;
export const Grouping = createGroups<TaskDescriptor>();
export const Tabbing = createTabs<TabTypes, TaskPagination>();

