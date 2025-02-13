
import { Tenant, TenantEntry, TenantStore, TenantEntryPagination, DialobTag, DialobForm, DialobSession, FormTechnicalName, TenantId, FormId, CreateFormRequest, DialobFormResponse } from './types';


export interface TenantStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'DIALOB' }): Promise<T>;
}

export class ImmutableTenantStore implements TenantStore {
  private _store: TenantStoreConfig;

  constructor(store: TenantStoreConfig) {
    this._store = store;
  }
  withStore(store: TenantStoreConfig): ImmutableTenantStore {
    return new ImmutableTenantStore(store);
  }

  async getTenants(): Promise<Tenant[]> {
    try {
      return await this._store.fetch<Tenant[]>(`dialob/api/tenants`, { repoType: 'DIALOB' });
    } catch (error) {
      console.error(error);
      return [];
    }
  }
  async getTenantEntries(id: string): Promise<TenantEntryPagination> {
    try {
      const forms = await this._store.fetch<TenantEntry[]>(`dialob/api/forms`, { repoType: 'DIALOB' });
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
    return await this._store.fetch<DialobTag[]>(`dialob/api/forms/${dialobFormId}/tags`, { repoType: 'DIALOB' });
  }
  async getDialobForm(dialobFormId: string): Promise<DialobForm> {
    return await this._store.fetch<DialobForm>(`dialob/api/forms/${dialobFormId}`, { repoType: 'DIALOB' });
  }
  async createDialobForm(formData: CreateFormRequest, tenantId?: string): Promise<DialobFormResponse> {
    return await this._store.fetch<DialobForm>(`api/forms?tenantId=${tenantId}`, {
      method: 'POST',
      body: JSON.stringify(formData),
      repoType: 'DIALOB'
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
    return await this._store.fetch<DialobForm>(`dialob/api/forms/${formName}?tenantId=${tenantId}`, { repoType: 'DIALOB' })
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
    return await this._store.fetch<any>(`dialob/api/forms/${formName}?tenantId=${tenantId}`, {
      method: 'DELETE',
      repoType: 'DIALOB'
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
      return await this._store.fetch<DialobSession[]>(`dialob/api/questionnaires/?formName=${props.technicalName}&tenantId=${props.tenantId}`, {
        repoType: 'DIALOB'
      })
    } catch (e) {
      console.log("falling back to typescript filtering", props);
      const result: DialobSession[] = await this._store.fetch<DialobSession[]>(`dialob/api/questionnaires`, { repoType: 'DIALOB' });
      return result.filter(q => q.metadata.formId === props.formId && q.metadata.tenantId === props.tenantId);
    }
  }
}