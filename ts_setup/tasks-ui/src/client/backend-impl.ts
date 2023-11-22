import { Backend, Store, Health } from './backend-types';
import type { TaskId, Task, TaskPagination, TaskStore, TaskUpdateCommand, CreateTask } from './task-types';
import { ProjectId, Project, ProjectPagination, ProjectStore, ProjectUpdateCommand, CreateProject } from './project-types';
import { Tenant, TenantEntry, TenantStore, TenantEntryPagination, DialobTag, DialobForm, DialobSession, FormTechnicalName, TenantId, FormId } from './tenant-types';
import { TenantConfig } from 'client';
import type { UserProfile } from './profile-types';
import type { User, Org } from './org-types';
import { mockOrg } from './client-mock';



export class ServiceImpl implements Backend {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  withTenantConfig(tenant: TenantConfig): ServiceImpl {
    return new ServiceImpl(this._store.withTenantConfig(tenant));
  }

  get config() { return this._store.config; }

  get tenant(): TenantStore {
    return {
      getTenantEntries: (tenantId: string) => this.getTenantEntries(tenantId),
      getTenants: () => this.getTenants(),
      getDialobTags: (dialobFormId: string) => this.getDialobTags(dialobFormId),
      getDialobForm: (dialobFormId: string) => this.getDialobForm(dialobFormId),
      getDialobSessions: (props: { formId: FormId, technicalName: FormTechnicalName, tenantId: TenantId }) => this.getDialobSessions(props)
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

  async health(): Promise<Health> {
    try {
      await this._store.fetch<{}>('config/health', { repoType: 'HEALTH' });
      const result: Health = { contentType: 'OK' };
      return result;
    } catch (error) {
      // thats ok, fallback to dialob
    }

    try {
      const tenantsUp = this._store.fetch<Tenant[]>(`api/tenants`, { repoType: 'EXT_DIALOB' });
      const result: Health = { contentType: 'DIALOB_EXT' };
      return result;
    } catch (error) {
      // thats ok, nothing else to check
    }
    const result: Health = { contentType: 'BACKEND_NOT_FOUND' };
    return result;
  }
  async currentTenant(): Promise<TenantConfig> {
    const current = await this._store.fetch<TenantConfig>(`config/current-tenants`, { repoType: 'CONFIG' });
    const { id, archived, created, documentType, name, preferences, repoConfigs, status, transactions, updated, version } = current;

    return { id, archived, created, documentType, name, preferences, repoConfigs, status, transactions, updated, version };
  }

  async currentUserProfile(): Promise<UserProfile> {
    const { today, user } = mockOrg;
    const { userId, userRoles: roles } = user;
    try {
      return { name: "", today, userId, roles };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      throw error;
    }
  }

  async getTenants(): Promise<Tenant[]> {
    return this._store.fetch<Tenant[]>(`api/tenants`, { repoType: 'EXT_DIALOB' });
  }
  async getTenantEntries(id: string): Promise<TenantEntryPagination> {
    const forms = await this._store.fetch<TenantEntry[]>(`api/forms`, { repoType: 'EXT_DIALOB' });
    return {
      page: 1,
      total: { pages: 1, records: forms.length },
      records: forms as any
    }
  }
  async getDialobTags(dialobFormId: string): Promise<DialobTag[]> {
    return await this._store.fetch<DialobTag[]>(`api/forms/${dialobFormId}/tags`, { repoType: 'EXT_DIALOB' });
  }
  async getDialobForm(dialobFormId: string): Promise<DialobForm> {
    return await this._store.fetch<DialobForm>(`api/forms/${dialobFormId}`, { repoType: 'EXT_DIALOB' });
  }
  async getDialobSessions(props: { formId: FormId, technicalName: FormTechnicalName, tenantId: TenantId }): Promise<DialobSession[]> {
    try {
      return await this._store.fetch<DialobSession[]>(`api/questionnaires/?formName=${props.technicalName}&tenantId=${props.tenantId}`, { repoType: 'EXT_DIALOB' })
    } catch (e) {
      console.log("falling back to typescript filtering", props);
      const result: DialobSession[] = await this._store.fetch<DialobSession[]>(`api/questionnaires`, { repoType: 'EXT_DIALOB' });
      return result.filter(q => q.metadata.formId === props.formId && q.metadata.tenantId === props.tenantId);
    }
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

  async getActiveProjects(): Promise<ProjectPagination> {
    const projects = await this._store.fetch<object[]>(`tenants`, { repoType: 'TENANT' });

    return {
      page: 1,
      total: { pages: 1, records: projects.length },
      records: projects as any
    }
  }

  getActiveProject(id: ProjectId): Promise<Project> {
    return this._store.fetch<Project>(`tenants/${id}`, { repoType: 'TENANT' });
  }

  async createProject(commands: CreateProject): Promise<Project> {
    return await this._store.fetch<Project>(`tenants`, {
      method: 'POST',
      body: JSON.stringify([commands]),
      repoType: 'TENANT'
    });
  }

  async updateActiveProject(id: ProjectId, commands: ProjectUpdateCommand<any>[]): Promise<Project> {
    return await this._store.fetch<Project>(`tenants/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'TENANT'
    });
  }


  async org(): Promise<{ org: Org, user: User }> {
    return mockOrg;
  }
}