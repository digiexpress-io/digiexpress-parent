import { TaskStore } from './backend-types';
import type { TaskId, Task, TaskPagination, TaskUpdateCommand, CreateTask } from './backend-types';



export interface TaskStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'TASKS' }): Promise<T>;
}


export class ImmutableTaskStore implements TaskStore {
  private _store: TaskStoreConfig;

  constructor(store: TaskStoreConfig) {
    this._store = store;
  }

  withStore(store: TaskStoreConfig): ImmutableTaskStore {
    return new ImmutableTaskStore(store);
  }

  async createTask(commands: CreateTask): Promise<Task> {
    return await this._store.fetch<Task>(`tasks`, {
      method: 'POST',
      body: JSON.stringify([commands]),
      repoType: 'TASKS'
    });
  }

  async updateActiveTask(id: TaskId, commands: TaskUpdateCommand<any>[]): Promise<Task> {
    return await this._store.fetch<Task>(`tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'TASKS'
    });
  }

  async getActiveTasks(): Promise<TaskPagination> {
    const tasks = await this._store.fetch<object[]>(`tasks`, { repoType: 'TASKS' });
    return {
      page: 1,
      total: { pages: 1, records: tasks.length },
      records: tasks as any
    }
  }

  getActiveTask(id: TaskId): Promise<Task> {
    return this._store.fetch<Task>(`tasks/${id}`, { repoType: 'TASKS' });
  }
}