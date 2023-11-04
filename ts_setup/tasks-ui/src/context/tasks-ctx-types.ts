import { Profile, Task } from 'client';
import { TaskDescriptor, PaletteType, TasksPaletteType, DescriptorState } from 'descriptor-task';


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