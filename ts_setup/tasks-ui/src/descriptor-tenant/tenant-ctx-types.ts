import { Profile } from 'client';
import { TenantEntryDescriptor, TenantPaletteType, PaletteType, DescriptorState } from './descriptor-types';

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
  entries: TenantEntryDescriptor[];
  palette: TenantPaletteType;
  profile: Profile;

  withEntries(): DescriptorState;
}