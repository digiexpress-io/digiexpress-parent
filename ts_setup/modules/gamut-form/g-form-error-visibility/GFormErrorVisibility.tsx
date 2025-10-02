import React from 'react';
import { useForm } from '@dxs-ts/gamut-api';
import { useScrollTo } from './useScrollTo';



export interface GFormErrorVisibilityContextType {
  isErrorsVisible: boolean;
  setErrorsVisible: () => void;
  register: (props: { id: string, ref: React.MutableRefObject<any> }) => void;
  unregister: (props: { id: string }) => void;
}

export const GFormErrorVisibilityContext = React.createContext<GFormErrorVisibilityContextType>({
  isErrorsVisible: false,
  setErrorsVisible: () => console.log('N/A'),
  register: () => { },
  unregister: () => { }
});

export const GFormErrorVisibilityProvider: React.FC<{ children: React.ReactNode, pageId: string }> = ({ children, pageId }) => {
  const { store, executionId } = useForm();
  const page = store.form.toPage(pageId);
  const [isErrorsVisible, setIsErrorsVisible] = React.useState<boolean>(false);
  const { refStore } = useScrollTo({ pageId, executionId });

  const setErrorsVisible = React.useCallback(() => {
    setIsErrorsVisible(true);
    refStore.consumed({ consumed: false })
  }, [pageId]);

  React.useEffect(() => {
    refStore.initPage(page);
  }, [page])


  const contextValue: GFormErrorVisibilityContextType = React.useMemo(() => {
    return {
      isErrorsVisible, setErrorsVisible,
      register: (props) => refStore.register(props),
      unregister: (props) => refStore.unregister(props)
    }
  }, [isErrorsVisible, setErrorsVisible, refStore])

  return (<GFormErrorVisibilityContext.Provider value={contextValue}>{children}</GFormErrorVisibilityContext.Provider>)
}

export const useGFormErrorVisibility = () => {
  const ctx: GFormErrorVisibilityContextType = React.useContext(GFormErrorVisibilityContext);
  return ctx;
}

