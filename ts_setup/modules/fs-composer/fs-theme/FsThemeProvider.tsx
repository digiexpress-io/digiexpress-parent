import React from 'react';

interface FsThemeContextType {
  isDarkMode: boolean;
  setIsDarkMode: (isDarkMode: boolean) => void;
}

const FsThemeContext = React.createContext<FsThemeContextType | undefined>(undefined);

export interface FsThemeProviderProps {
  children: React.ReactNode;
}

export const FsThemeProvider: React.FC<FsThemeProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);

  const contextValue: FsThemeContextType = React.useMemo(() => ({
    isDarkMode,
    setIsDarkMode,
  }), [isDarkMode]);

  console.log("XXX")

  return (
    <FsThemeContext.Provider value={contextValue}>
      {props.children}
    </FsThemeContext.Provider>
  );
};

export function useFsTheme(): FsThemeContextType {
  const result = React.useContext(FsThemeContext);
  if (!result) {
    throw new Error('FsThemeContext is not created!');
  }
  return result;
}
