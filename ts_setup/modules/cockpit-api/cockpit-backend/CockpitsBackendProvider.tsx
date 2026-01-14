import React from 'react';
import { CockpitApi } from '../cockpit-types';

export interface CockpitsBackendContextType {
  deps: any[];
  currentUser: {
    name: string;
    email: string;
  },
  roles: any[]; 
  permissions: {
    isCreateCockpitAllowed: boolean;
    isModifyCockpitAllowed: boolean;
    isDeleteCockpitAllowed: boolean;
  };
  features: {
    isCockpitActivityEnabled: boolean;
  },
  navigate: {
    findAllCockpits: () => void;
  };
  persistence: {
    findAllCockpits: () => Promise<CockpitApi.CockpitContainer[]>;
  },
  slots: {
    DateTimeFormatter: React.ElementType<{ value: string | Date | undefined, variant?: 'text' }>;
    DateTimePicker: React.ElementType<{
      label?: string | React.ReactNode,
      readonly?: boolean,
      fullWidth?: boolean,
      value: string | Date | undefined | null;
      onChange?: (newValue: Date | null) => void;
      onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
    }>
  }
}

export const CockpitsBackendContext = React.createContext<CockpitsBackendContextType>({} as any);

export interface CockpitsBackendProviderProps {
  deps: any[];
  children: React.ReactNode;
  currentUser: CockpitsBackendContextType['currentUser'];
  roles: CockpitsBackendContextType['roles'];
  navigate: CockpitsBackendContextType['navigate'];
  slots: CockpitsBackendContextType['slots'];
  persistence: CockpitsBackendContextType['persistence'];
  permissions: CockpitsBackendContextType['permissions'];
  features: CockpitsBackendContextType['features'];
}

export const CockpitsBackendProvider: React.FC<CockpitsBackendProviderProps> = (props) => {
  const { navigate, persistence, permissions, currentUser, roles, slots, features, deps } = props;

  const contextValue: CockpitsBackendContextType = React.useMemo(() => {
    return { navigate, persistence, permissions, currentUser, roles, slots, features, deps };
  }, [roles, currentUser, deps]);

  return (<CockpitsBackendContext.Provider value={contextValue}>{props.children}</CockpitsBackendContext.Provider>);
}

export function useCockpitsBackend() {
  const result: CockpitsBackendContextType = React.useContext(CockpitsBackendContext);
  return result;
}