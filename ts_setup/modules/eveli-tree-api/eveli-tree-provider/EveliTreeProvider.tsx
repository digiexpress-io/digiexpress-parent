import React from 'react';

export interface EveliTreeContextType {
  isDarkMode: boolean;
  setIsDarkMode: (isDarkMode: boolean) => void;
}

export const EveliTreeContext = React.createContext<EveliTreeContextType | undefined>(undefined);

export interface EveliTreeProviderProps {
  children: React.ReactNode;
}

export const EveliTreeProvider: React.FC<EveliTreeProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);

  const contextValue: EveliTreeContextType = React.useMemo(() => {
    return { isDarkMode, setIsDarkMode };
  }, [isDarkMode]);

  return (
    <EveliTreeContext.Provider value={contextValue}>
      {props.children}
    </EveliTreeContext.Provider>
  );
};

export function useEveliTree() {
  const result = React.useContext(EveliTreeContext);
  if (!result) {
    throw new Error('EveliTreeContext is not created!');
  }
  return result;
}