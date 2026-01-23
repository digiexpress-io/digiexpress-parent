
import React from 'react';
import { ThemeProvider, createTheme, ThemeOptions, Theme } from '@mui/material';
import { CockpitStore } from '@dxs-ts/gamut-cockpit-store';


export interface GThemeProviderContextType {
  theme: Theme;
  themeOptionsPrimary: ThemeOptions;
  secondaryThemeOptions: Record<string, ThemeOptions>;

  setThemeOptions: (themeOptions?: string) => void;
}

export const GThemeProviderContext = React.createContext<GThemeProviderContextType | undefined>(undefined);

export interface GThemeProviderProps {
  themeOptions: ThemeOptions;
  secondaryThemeOptions?: Record<string, ThemeOptions>;
  children: React.ReactNode
}

export const GThemeProvider: React.FC<GThemeProviderProps> = (props) => {  

  const themeOptionsPrimary = props.themeOptions;
  const [secondaryOptions, setSecondaryOptions] = React.useState<SecondaryOptions>(() => _getSecondaryOptions(props));

  const setThemeOptions: GThemeProviderContextType['setThemeOptions'] = React.useCallback((themeOptions) => {
    setSecondaryOptions({reload: true, value: _getSecondaryOptions(props, themeOptions).value });
  }, []);

  const contextValue: GThemeProviderContextType = React.useMemo(() => {
    const theme = createTheme(secondaryOptions?.value ?? themeOptionsPrimary);
    const secondaryThemeOptions: GThemeProviderContextType['secondaryThemeOptions'] = props.secondaryThemeOptions ?? {};

    return { 
      theme, 
      themeOptionsPrimary,
      secondaryThemeOptions,
      setThemeOptions
    };
  }, [secondaryOptions.reload]);

  return (
    <GThemeProviderContext.Provider value={contextValue}>
      <ThemeProvider theme={contextValue.theme}>
        {props.children}
      </ThemeProvider>
    </GThemeProviderContext.Provider>);
}

export function useGTheme() {
  const result = React.useContext(GThemeProviderContext);
  if(!result) {
    throw new Error('GThemeProviderContext is not created!');
  }
  return result;
}


function _getSecondaryOptions(props: GThemeProviderProps, themeOptions?: string): SecondaryOptions {
  const cockpit = CockpitStore.get();
  const secondary = props.secondaryThemeOptions ?? {};

  return {
    reload: false, 
    value: cockpit ? secondary[themeOptions ?? cockpit?.cockpitConfigName] : undefined
  }
}

interface SecondaryOptions {
  reload: boolean;
  value: ThemeOptions | undefined;
}