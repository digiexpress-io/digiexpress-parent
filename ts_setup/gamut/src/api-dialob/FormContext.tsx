import React from 'react'

import { DialobApi } from './dialob-types';
import { useFormStore } from './useFormStore';
import { OfferApi, useOffers } from '../api-offer';
import { useIam } from '../api-iam';
import { GFormUnavailable } from '../g-form-unavailable';


export const FormContext = React.createContext<DialobApi.FormContextType>({} as any);

export interface FormProviderProps {
  executionId: string;
  variant: string;
  onAfterComplete: () => void;
  children: React.ReactNode;
}

export const FormProvider: React.FC<FormProviderProps> = (props) => {
  const { authType } = useIam();
  const offers = useOffers();
  const [offer, setOffer] = React.useState<OfferApi.Offer>();
  React.useEffect(() => {
    
    if(authType === 'ANON') {
      offers.fetchOffer(props.executionId).then(setOffer);
    } else {
      const offer = offers.getOffer(props.executionId);
      setOffer(offer);
    }
  }, [props.executionId, authType, offers]);

  const formId = offer?.formId;

  if (!offer || !formId) {
    return <>Failed to load form...</>;
  }
  return (<WithFormProvider {...props} id={formId}>{props.children}</WithFormProvider>);
}

const WithFormProvider: React.FC<FormProviderProps & { id: string }> = (props) => {
  
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

  if(store.pendingError) {
    return <GFormUnavailable />
  }

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