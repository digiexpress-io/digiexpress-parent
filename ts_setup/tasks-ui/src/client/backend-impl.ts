import { Backend, Store } from './backend-types';
import type { TaskId, Task, TaskPagination, TaskStore, TaskUpdateCommand, CreateTask } from './task-types';
import { ProjectId, Project, ProjectPagination, ProjectStore, ProjectUpdateCommand, CreateProject } from './project-types';
import { Tenant, TenantEntry, TenantStore, TenantEntryPagination, DialobTag, DialobForm } from './tenant-types';

import type { Profile, ProfileStore } from './profile-types';
import type { User, Org } from './org-types';
import { mockOrg } from './client-mock';



type BackendInit = { created: boolean } | null


export class ServiceImpl implements Backend {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  withProjectId(projectId: string): ServiceImpl {
    return new ServiceImpl(this._store.withProjectId(projectId));
  }

  get config() { return this._store.config; }

  get profile(): ProfileStore {
    return {
      getProfile: () => this.getProfile(),
      createProfile: () => this.createProfile()
    }
  }
  async getProfile(): Promise<Profile> {
    if (!this._store.config.performInitCheck) {
      return { name: "", contentType: "OK", today: new Date(), userId: '', roles: [] };
    }

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
  get tenant(): TenantStore {
    return {
      getTenantEntries: (tenantId: string) => this.getTenantEntries(tenantId),
      getTenants: () => this.getTenants(),
      getDialobTags: (dialobFormId: string) => this.getDialobTags(dialobFormId),
      getDialobForm: (dialobFormId: string) => this.getDialobForm(dialobFormId),
    };
  }
  get task(): TaskStore {
    return {
      getActiveTasks: () => this.getActiveTasks(),
      getActiveTask: (id: TaskId) => this.getActiveTask(id),
      updateActiveTask: (id: TaskId, commands: TaskUpdateCommand<any>[]) => this.updateActiveTask(id, commands),
      createTask: (commands: CreateTask) => this.createTask(commands),
    };
  }
  get project(): ProjectStore {
    return {
      getActiveProjects: () => this.getActiveProjects(),
      getActiveProject: (id: ProjectId) => this.getActiveProject(id),
      updateActiveProject: (id: ProjectId, commands: ProjectUpdateCommand<any>[]) => this.updateActiveProject(id, commands),
      createProject: (commands: CreateProject) => this.createProject(commands),
    };
  }

  getTenants(): Promise<Tenant[]> {
    return this._store.fetch<Tenant[]>(`api/tenants`);
  }
  async getTenantEntries(id: string): Promise<TenantEntryPagination> {
    const forms = await this._store.fetch<TenantEntry[]>(`api/forms`);
    return {
      page: 1,
      total: { pages: 1, records: forms.length },
      records: forms as any
    }
  }

  async getDialobTags(dialobFormId: string): Promise<DialobTag[]> {
    return await this._store.fetch<DialobTag[]>(`api/forms/${dialobFormId}/tags`);
  }
  async getDialobForm(dialobFormId: string): Promise<DialobForm> {
    return await this._store.fetch<DialobForm>(`api/forms/${dialobFormId}`);
  }



  async createTask(commands: CreateTask): Promise<Task> {
    return await this._store.fetch<Task>(`tasks`, {
      method: 'POST',
      body: JSON.stringify([commands])
    });
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

  async getActiveProjects(): Promise<ProjectPagination> {
    const projects = await this._store.fetch<object[]>(`projects`);

    ///const projects = Object.values(mockProjects);
    return {
      page: 1,
      total: { pages: 1, records: projects.length },
      records: projects as any
    }
  }

  getActiveProject(id: ProjectId): Promise<Project> {
    return this._store.fetch<Project>(`projects/${id}`);
  }

  async createProject(commands: CreateProject): Promise<Project> {
    return await this._store.fetch<Project>(`projects`, {
      method: 'POST',
      body: JSON.stringify([commands])
    });
  }

  async updateActiveProject(id: ProjectId, commands: ProjectUpdateCommand<any>[]): Promise<Project> {
    return await this._store.fetch<Project>(`projects/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands)
    });
  }


  async org(): Promise<{ org: Org, user: User }> {
    return mockOrg;
  }
}