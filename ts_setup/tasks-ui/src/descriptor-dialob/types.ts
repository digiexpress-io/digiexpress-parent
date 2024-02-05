

export type TenantId = string;
export type FormTechnicalName = string;
export type FormTitle = string;
export type SessionId = string;
export type FormId = string;
export type DialobErrorTypes = "CREATE_FORM_ERROR" | "DELETE_FORM_ERROR";

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


export interface DialobSession {
  id: SessionId,
  metadata: {
    formId: string,
    status: "NEW" | "OPEN" | "COMPLETED",
    tenantId: TenantId,
    created: Date,
    lastAnswer: Date,
    owner: string,
  }
}

export interface DialobForm {
  _id: FormId;
  name: FormTechnicalName;
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

export interface CreateFormRequest {
  name: FormTechnicalName;
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

export interface DialobFormResponse {
  status: string;
  error?: DialobError;
  form?: DialobForm;
}

export interface DialobError {
  type: DialobErrorTypes;
  message: string;
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
  getDialobSessions(props: { formId: FormId, technicalName: FormTechnicalName, tenantId: TenantId }): Promise<DialobSession[]>;
  createDialobForm(formData: CreateFormRequest, tenantId?: string): Promise<DialobFormResponse>;
  copyDialobForm(formName: string, newFormName: string, newFormTitle: string, tenantId?: string): Promise<DialobFormResponse>;
  deleteDialobForm(formName: string, tenantId?: string): Promise<void>;
}



export interface TenantEntryDescriptor {
  tenantId: string;
  source: TenantEntry;
  formName: FormTechnicalName;
  formTitle: FormTitle;
  created: Date;
  lastSaved: Date;
  sessions?: DialobSession[];
}

export interface TenantDescriptor {
  source: Tenant;
}

export interface TenantState {
  tenantEntries: TenantEntryDescriptor[];
  tenants: TenantDescriptor[];
  activeTenant: TenantId | undefined;
}

export interface TenantContextType {
  withActiveTenant(tenantId?: TenantId): void;
  withTenants(tenants: Tenant[]): void;
  withTenantEntries(tenantEntries: TenantEntry[]): void;
  reload: () => Promise<void>;

  loading: boolean;
  state: TenantState;
}


