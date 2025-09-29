import React from 'react';


export interface GFormErrorVisibilityContextType {
  setErrorsVisible: () => void;
  isErrorsVisible: boolean;
}

export const GFormErrorVisibilityContext = React.createContext<GFormErrorVisibilityContextType>({
  isErrorsVisible: false,
  setErrorsVisible: () => console.log('N/A') 
});

export const GFormErrorVisibilityProvider: React.FC<{ children: React.ReactNode, pageId: string }> = ({ children }) => { 
  const [isErrorsVisible, setIsErrorsVisible] = React.useState<boolean>(false);
  const setErrorsVisible = React.useCallback(() => {
    setIsErrorsVisible(true);
  }, []);

  const contextValue: GFormErrorVisibilityContextType = React.useMemo(() => {
    return { isErrorsVisible, setErrorsVisible }
  }, [isErrorsVisible, setErrorsVisible])

  return (<GFormErrorVisibilityContext.Provider value={contextValue}>{children}</GFormErrorVisibilityContext.Provider>)
}

export const useGFormErrorVisibility = () => {
  const ctx: GFormErrorVisibilityContextType = React.useContext(GFormErrorVisibilityContext);
  return ctx;
}

