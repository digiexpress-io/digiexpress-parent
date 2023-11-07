import { Profile } from 'client';
import { TenantEntryDescriptor, TenantPaletteType, PaletteType, DescriptorState, TenantDescriptor } from './descriptor-types';
import { TenantId, FormTechnicalName, Tenant, TenantEntry } from './descriptor-types';


export interface TenantContextType {
  setState: TenantDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: TenantState,
  palette: PaletteType;
}

export type TenantMutator = (prev: TenantState) => TenantState;
export type TenantDispatch = (mutator: TenantMutator) => void;

export interface TenantState {
  tenantEntries: TenantEntryDescriptor[];
  tenants: TenantDescriptor[];
  activeTenant: TenantId | undefined;
  activeTenantEntry: FormTechnicalName | undefined;

  palette: TenantPaletteType;
  profile: Profile;

  withProfile(profile: Profile): TenantState;

  withActiveTenant(tenantId?: TenantId): TenantState;
  withActiveTenantEntry(id?: FormTechnicalName): TenantState;

  withTenants(tenants: Tenant[]): TenantState;
  withTenantEntries(tenantEntries: TenantEntry[]): TenantState;
}