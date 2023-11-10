
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

export interface DialobTag {
  id: string;
  name: string;
  formName: string;
}

export interface DialobVariable {
  context: string | undefined, // defined if context variable, undefined if expression variable
  contextType: string,
  defaultValue: any | null | undefined,
  name: string
}

export interface DialobForm {
  _id: string;
  name: string;
  metadata: {
    label: string;
    languages: string[];
    labels?: string[];
  },
  data: Record<string, {
    id: string,
    type: string,
    label?: Record<string, string>, // locale-locale label
  }>,
  variables?: DialobVariable[];
}


export interface TenantEntryPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: TenantEntry[];
}

export interface TenantStore {
  getTenantEntries(tenantId: string): Promise<TenantEntryPagination>
  getTenants(): Promise<Tenant[]>
  getDialobTags(dialobFormId: string): Promise<DialobTag[]>;
  getDialobForm(dialobFormId: string): Promise<DialobForm>;
}

