
import { TenantEntry, FormTechnicalName, FormTitle, Tenant, TenantId, UserProfile, DialobSession } from 'client';

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


export interface Data {
  tenantEntries: TenantEntryDescriptor[];
  palette: TenantPaletteType;
  profile: UserProfile;
}

export interface TenantGroupsAndFilters {
  groupBy: GroupBy;
  groups: Group[];
  searchString: string | undefined;

  withEntries(entries: TenantEntryDescriptor[]): TenantGroupsAndFilters;
  withData(entries: Data): TenantGroupsAndFilters;
  withGroupBy(groupBy: GroupBy): TenantGroupsAndFilters;
  withSearchString(searchString: string): TenantGroupsAndFilters;
}


export interface TenantState {
  tenantEntries: TenantEntryDescriptor[];
  tenants: TenantDescriptor[];
  activeTenant: TenantId | undefined;
  activeTenantEntry: FormTechnicalName | undefined;
  palette: TenantPaletteType;
  profile: UserProfile;

  withProfile(profile: UserProfile): TenantState;
  withActiveTenant(tenantId?: TenantId): TenantState;
  withActiveTenantEntry(id?: FormTechnicalName): TenantState;
  withTenants(tenants: Tenant[]): TenantState;
  withTenantEntries(tenantEntries: TenantEntry[]): TenantState;
  toGroupsAndFilters(): TenantGroupsAndFilters;
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

