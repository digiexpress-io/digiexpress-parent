
import React from 'react';


export interface TableThemeContextType {
  rowProps?: {
    height: string
  }
}

export const TableThemeContext = React.createContext<TableThemeContextType | undefined>(undefined);

export interface TableThemeProviderProps {
  children: React.ReactNode
  rowProps?: TableThemeContextType['rowProps']
}


export const TableThemeProvider: React.FC<TableThemeProviderProps> = (props) => {
  const { rowProps } = props;
  const contextValue: TableThemeContextType | undefined = React.useMemo(() => {
    return { rowProps };
  }, [rowProps]);

  return (<TableThemeContext.Provider value={contextValue}>{props.children}</TableThemeContext.Provider>);
}

export function useTableTheme() {
  const result = React.useContext(TableThemeContext);
  if(!result) {
    throw new Error('TableThemeContext is not created!');
  }
  return result;
}
