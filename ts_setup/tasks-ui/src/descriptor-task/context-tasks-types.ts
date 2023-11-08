import { Profile, Task } from 'client';
import { DescriptorState, TaskDescriptor, TasksPaletteType, PaletteType } from './types';

export interface TasksContextType {
  setState: TasksDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: TasksState,
  palette: PaletteType;


}

export type TasksMutator = (prev: TasksState) => TasksState;
export type TasksDispatch = (mutator: TasksMutator) => void;

export interface TasksState {
  tasks: TaskDescriptor[];
  tasksByOwner: Record<string, TaskDescriptor[]>;
  palette: TasksPaletteType;
  profile: Profile;
  roles: string[];
  owners: string[];

  withProfile(profile: Profile): TasksState;
  withTasks(tasks: Task[]): TasksState;
  withDescriptors(): DescriptorState;
}