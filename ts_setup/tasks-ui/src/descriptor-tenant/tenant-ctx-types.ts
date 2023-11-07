import { PaletteType, TenantState } from './descriptor-types';


export interface TenantContextType {
  setState: TenantDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: TenantState,
  palette: PaletteType;
}

export type TenantMutator = (prev: TenantState) => TenantState;
export type TenantDispatch = (mutator: TenantMutator) => void;

