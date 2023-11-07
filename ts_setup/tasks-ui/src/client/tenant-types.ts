
export type TenantId = string;
export type FormTechnicalName = string;
export type FormTitle = string;

export interface Tenant {
  id: TenantId;
  name: string;
}
export interface TenantEntry {
  id: FormTechnicalName; //technicalName, (resys tenant: MyTenantTestForm)
  metadata: {
    label: FormTitle; //formName, (resys tenant: TenantTestForm)
    created: string;
    lastSaved: string;
    tenantId: TenantId;
  }
}

export interface TenantEntryPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: TenantEntry[];
}

export interface TenantStore {
  getTenantEntries(tenantId: string): Promise<TenantEntryPagination>
  getTenants(): Promise<Tenant[]>
}

