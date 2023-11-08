
import { TenantEntry, FormTechnicalName, FormTitle, Tenant, TenantId, Profile } from 'client';

export interface PaletteType {
  colors: { red: string, green: string, yellow: string, blue: string, violet: string }
}

export interface TenantPaletteType { }
export type GroupBy = 'tenant' | 'none';
export interface Group {
  id: string;
  type: GroupBy;
  color?: string;
  records: TenantEntryDescriptor[];
}


export interface TenantEntryDescriptor {
  source: TenantEntry;
  formName: FormTechnicalName;
  formTitle: FormTitle;
  created: Date;
  lastSaved: Date;
}

export interface TenantDescriptor {
  source: Tenant;
}

export interface TenantState {
  tenantEntries: TenantEntryDescriptor[];
  tenants: TenantDescriptor[];
  activeTenant: TenantId | undefined;
  activeTenantEntry: FormTechnicalName | undefined;
  groups: Group[];
  groupBy: GroupBy;
  palette: TenantPaletteType;
  profile: Profile;
  filtered: TenantEntryDescriptor[];
  searchString: string | undefined;


  withSearchString(searchString: string): TenantState;
  withProfile(profile: Profile): TenantState;

  withActiveTenant(tenantId?: TenantId): TenantState;
  withActiveTenantEntry(id?: FormTechnicalName): TenantState;

  withTenants(tenants: Tenant[]): TenantState;
  withTenantEntries(tenantEntries: TenantEntry[]): TenantState;
}

export interface TenantContextType {
  setState: TenantDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: TenantState,
  palette: PaletteType;
}

export type TenantMutator = (prev: TenantState) => TenantState;
export type TenantDispatch = (mutator: TenantMutator) => void;

