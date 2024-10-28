import React from 'react';



interface LocaleSelectContextType {
  locale: string;
  setLocale: (newValue: string) => void;
}

export const LocaleSelectContext = React.createContext({} as any);


export const LocaleSelectContextProvider: React.FC<{ locale: string, children: React.ReactElement }> = (props) => {
  const [locale, setLocale] = React.useState(props.locale);

  const contextValue: LocaleSelectContextType = React.useMemo(() => {

    return {
      locale, setLocale
    };
  }, [locale, setLocale])

  return (<LocaleSelectContext.Provider value={contextValue}>{props.children}</LocaleSelectContext.Provider>)
}

export function useLocaleSelect() {
  const ctx: LocaleSelectContextType = React.useContext(LocaleSelectContext);
  return ctx;
}

