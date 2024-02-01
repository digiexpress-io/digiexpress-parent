
import { TenantEntry, FormTechnicalName, FormTitle, Tenant, TenantId, DialobSession } from 'client';

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


