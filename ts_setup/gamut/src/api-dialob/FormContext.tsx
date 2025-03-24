import React from 'react'

import { DialobApi } from './dialob-types';
import { useFormStore } from './useFormStore';



export const FormContext = React.createContext<DialobApi.FormContextType>({} as any);

export interface FormProviderProps {
  id: string;
  executionId: string;
  variant: string;
  onAfterComplete: () => void;
  children: React.ReactNode;
}

export const FormProvider: React.FC<FormProviderProps> = (props) => {

  const { id, executionId, variant, onAfterComplete } = props;
  const store = useFormStore({ id });
  const contextValue = React.useMemo(() => Object.freeze({ store, variant, executionId, onAfterComplete }), [store, variant, executionId, onAfterComplete])

  React.useEffect(() => {
    if(store.pending) {
      return;
    }

    if(store.form.state.completed) {
      contextValue.onAfterComplete(); //complete signal from backend is received
    }
  }, [store, contextValue]);

  return (<FormContext.Provider value={contextValue}>{props.children}</FormContext.Provider>);
}


export const useForm = () => {
  const result: DialobApi.FormContextType = React.useContext(FormContext);
  return result;
}
export function useFormTip(): DialobApi.ActionItem | undefined {
  const { store } = useForm();
  return store.form.tip;
}