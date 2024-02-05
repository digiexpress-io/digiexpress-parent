import { Backend, Store, Health } from './backend-types';
import { ProjectId, Project, ProjectPagination, ProjectStore, ProjectUpdateCommand, CreateProject } from './project-types';
import { Tenant, TenantEntry, TenantStore, TenantEntryPagination, DialobTag, DialobForm, DialobSession, FormTechnicalName, TenantId, FormId, CreateFormRequest, DialobFormResponse } from './tenant-types';
import { TenantConfig } from 'client';
import type { UserProfileAndOrg, UserProfileStore, UserProfile, UserProfileUpdateCommand } from './profile-types';
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

  get store() { return this._store }
  get config() { return this._store.config; }

  get tenant(): TenantStore {
    return {
      getTenantEntries: (tenantId: string) => this.getTenantEntries(tenantId),
      getTenants: () => this.getTenants(),
      getDialobTags: (dialobFormId: string) => this.getDialobTags(dialobFormId),
      getDialobForm: (dialobFormId: string) => this.getDialobForm(dialobFormId),
      getDialobSessions: (props: { formId: FormId, technicalName: FormTechnicalName, tenantId: TenantId }) => this.getDialobSessions(props),
      createDialobForm: (formData: CreateFormRequest, tenantId?: string) => this.createDialobForm(formData, tenantId),
      copyDialobForm: (formName: string, newFormName: string, newFormTitle: string, tenantId?: string) => this.copyDialobForm(formName, newFormName, newFormTitle, tenantId),
      deleteDialobForm: (formName: string, tenantId?: string) => this.deleteDialobForm(formName, tenantId),
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
  get userProfile(): UserProfileStore {
    return {
      getUserProfileById: (id: string) => this.getUserProfile(id),
      findAllUserProfiles: () => this.findUserProfiles(),
      updateUserProfile: (profileId: string, commands: UserProfileUpdateCommand<any>[]) => this.updateActiveUserProfile(profileId, commands)
    };
  }
  async getUserProfile(id: string): Promise<UserProfile> {
    return await this._store.fetch<UserProfile>(`userprofiles/${id}`, { repoType: 'USER_PROFILE' });
  }
  async findUserProfiles(): Promise<UserProfile[]> {
    return await this._store.fetch<UserProfile[]>(`userprofiles`, { repoType: 'USER_PROFILE' });
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

  async currentUserProfile(): Promise<UserProfileAndOrg> {
    const { today, org } = mockOrg;

    try {
      const user = await this._store.fetch<UserProfile>(`config/current-user-profile`, { repoType: 'CONFIG' })
      return {
        user,
        userId: user.id,
        today,
        roles: Object.keys(org.roles)
      };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      throw error;
    }
  }

  async getTenants(): Promise<Tenant[]> {
    try {
      return await this._store.fetch<Tenant[]>(`api/tenants`, { repoType: 'EXT_DIALOB' });
    } catch (error) {
      console.error(error);
      return [];
    }
  }
  async getTenantEntries(id: string): Promise<TenantEntryPagination> {
    try {
      const forms = await this._store.fetch<TenantEntry[]>(`api/forms`, { repoType: 'EXT_DIALOB' });
      return {
        page: 1,
        total: { pages: 1, records: forms.length },
        records: forms as any
      }
    } catch (error) {
      console.error(error);
      return {
        page: 1,
        total: { pages: 1, records: 0 },
        records: []
      }
    }
  }
  async getDialobTags(dialobFormId: string): Promise<DialobTag[]> {
    return await this._store.fetch<DialobTag[]>(`api/forms/${dialobFormId}/tags`, { repoType: 'EXT_DIALOB' });
  }
  async getDialobForm(dialobFormId: string): Promise<DialobForm> {
    return await this._store.fetch<DialobForm>(`api/forms/${dialobFormId}`, { repoType: 'EXT_DIALOB' });
  }
  async createDialobForm(formData: CreateFormRequest, tenantId?: string): Promise<DialobFormResponse> {
    return await this._store.fetch<DialobForm>(`api/forms?tenantId=${tenantId}`, {
      method: 'POST',
      body: JSON.stringify(formData),
      repoType: 'EXT_DIALOB'
    })
      .then(form => {
        return { status: 'OK', form };
      })
      .catch(ex => {
        console.log("Form create failed", ex);
        return { status: 'ERROR', error: { message: "dialob.error.already.exists", type: "CREATE_FORM_ERROR" } };
      });
  }
  async copyDialobForm(formName: string, newFormName: string, newFormTitle: string, tenantId?: string): Promise<DialobFormResponse> {
    return await this._store.fetch<DialobForm>(`api/forms/${formName}?tenantId=${tenantId}`, { repoType: 'EXT_DIALOB' })
      .then(
        formData => {
          const newForm = JSON.parse(JSON.stringify(formData));
          delete newForm._id;
          delete newForm._rev;
          newForm.name = newFormName;
          Object.assign(newForm.metadata, {
            label: newFormTitle,
            lastSaved: null,
            created: new Date()
          });
          return this.createDialobForm(newForm as CreateFormRequest, tenantId);
        }
      );
  }
  async deleteDialobForm(formName: string, tenantId?: string): Promise<void> {
    return await this._store.fetch<any>(`api/forms/${formName}?tenantId=${tenantId}`, {
      method: 'DELETE',
      repoType: 'EXT_DIALOB'
    })
      .then((response) => {
        if (response.ok) {
          console.log("Form deleted")
        } else {
          console.log("Form delete failed", response);
        }
      })
      .catch(ex => console.log("Form delete failed", ex));
  }
  async getDialobSessions(props: { formId: FormId, technicalName: FormTechnicalName, tenantId: TenantId }): Promise<DialobSession[]> {
    try {
      return await this._store.fetch<DialobSession[]>(`api/questionnaires/?formName=${props.technicalName}&tenantId=${props.tenantId}`, {
        repoType: 'EXT_DIALOB'
      })
    } catch (e) {
      console.log("falling back to typescript filtering", props);
      const result: DialobSession[] = await this._store.fetch<DialobSession[]>(`api/questionnaires`, { repoType: 'EXT_DIALOB' });
      return result.filter(q => q.metadata.formId === props.formId && q.metadata.tenantId === props.tenantId);
    }
  }

  async updateActiveUserProfile(profileId: string, commands: UserProfileUpdateCommand<any>[]): Promise<UserProfile> {
    return await this._store.fetch<UserProfile>(`userprofiles/${profileId}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'USER_PROFILE'
    });
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
    const user = await this._store.fetch<UserProfile>(`config/current-user-profile`, { repoType: 'CONFIG' })
    return {
      org: mockOrg.org, user: {
        userId: user.id,
        activity: [],
        avatar: '',
        displayName: '',
        type: 'TASK_USER',
        userRoles: Object.keys(mockOrg.org.roles)
      }
    };
  }
}