import React from "react";



export interface TableProviderContextType {
  persistent: boolean;
  save(settingsId: string, config: TableConfig[]): Promise<any>;
  find(settingsId: string): Promise<TableSettings | undefined>;
}

export interface TableSettings {
  settingsId: string;
  config: TableConfig[];
}
export interface TableConfig {
  dataId: string;
  label: string | undefined;
  value: string;
}

export const TableProviderContext = React.createContext<TableProviderContextType>({} as any)

// just storage context
export const TableProvider: React.FC<{ 
  persistent?: { save: TableProviderContextType['save'], find: TableProviderContextType['find'] };
  children: React.ReactNode;
  }> = ({ persistent, children }) => {
  
  const context: TableProviderContextType = React.useMemo(() => {
    return { 
      persistent: !!persistent, 
      save: persistent?.save ?? (async () => { return {} as any}),
      find: persistent?.find ?? (async () => { return undefined })
    }
  }, [persistent])
  return (<TableProviderContext.Provider value={context}>{children}</TableProviderContext.Provider>);
}

export function useTable() {
  const context: TableProviderContextType = React.useContext(TableProviderContext);
  return context;
}
