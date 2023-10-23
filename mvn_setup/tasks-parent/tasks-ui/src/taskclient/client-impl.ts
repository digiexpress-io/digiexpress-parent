import { Backend, Store } from './client-types';
import type { TaskId, Task, TaskPagination, TaskStore, TaskUpdateCommand } from './task-types';
import type { Profile, ProfileStore } from './profile-types';
import type { User, Org } from './org-types';
import { mockOrg } from './client-mock';



type BackendInit = { created: boolean } | null


export class ServiceImpl implements Backend {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  get config() { return this._store.config; }

  get profile(): ProfileStore {
    return {
      getProfile: () => this.getProfile(),
      createProfile: () => this.createProfile()
    }
  }
  async getProfile(): Promise<Profile> {
    const { today, user } = mockOrg;
    const { userId, userRoles: roles } = user;
    try {
      const init = await this._store.fetch<BackendInit>("init", { notFound: () => null });
      if (init === null) {
        return { name: "", contentType: "BACKEND_NOT_FOUND", today, userId, roles };
      }

      return { name: "", contentType: "OK", today, userId, roles };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      return { name: "", contentType: "NO_CONNECTION", today, userId, roles };
    }
  }

  createProfile(): Promise<Profile> {
    return this._store.fetch<Profile>("head", { method: "POST", body: JSON.stringify({}) });
  }

  get task(): TaskStore {
    return {
      getActiveTasks: () => this.getActiveTasks(),
      getActiveTask: (id: TaskId) => this.getActiveTask(id),
      updateActiveTask: (id: TaskId, commands: TaskUpdateCommand<any>[]) => this.updateActiveTask(id, commands)
    };
  }

  async updateActiveTask(id: TaskId, commands: TaskUpdateCommand<any>[]): Promise<Task> {
    return await this._store.fetch<Task>(`tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands)
    });
  }

  async getActiveTasks(): Promise<TaskPagination> {
    const tasks = await this._store.fetch<object[]>(`tasks`);
    return {
      page: 1,
      total: { pages: 1, records: tasks.length },
      records: tasks as any
    }
  }
  getActiveTask(id: TaskId): Promise<Task> {
    return this._store.fetch<Task>(`tasks/${id}`);
  }
  async org(): Promise<{ org: Org, user: User }> {
    return mockOrg;
  }
}