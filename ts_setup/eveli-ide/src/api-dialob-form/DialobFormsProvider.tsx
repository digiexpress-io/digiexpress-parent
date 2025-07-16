import React from 'react';
import { useQuery } from "@tanstack/react-query";

import { DashboardState, DasboardItem } from './types-dashboard';
import { 
  Visitor_DeleteForm, Visitor_CopyForm, Visitor_CreateNewForm, 
  Visitor_DashboardState, Visitor_DownloadAllForms, 
  Visitor_RestApi, Visitor_UploadCsvForm, Visitor_UploadFormJson 
} from './visitors';
import { DialobRestApi } from './types-rest-api';


export interface DialobFormsOperationResult {
  success: boolean;
  message: string;
}

export interface DialobFormsContextType {
  forms: DasboardItem[];
  uploadJsonForm: (file: File) => Promise<DialobFormsOperationResult>;
  uploadCsvForm: (file: File) => Promise<DialobFormsOperationResult>;
  downloadAllForms: (forms: DialobRestApi.FormListItem[]) => Promise<{ blob: Blob, fileName: string }>;

  createForm: (props: Visitor_CreateNewForm.Input) => Promise<Visitor_CreateNewForm.Result>;
  copyForm: (props: Visitor_CopyForm.Input) => Promise<Visitor_CopyForm.Result>;
  deleteForm: (props: Visitor_DeleteForm.Input) => Promise<Visitor_DeleteForm.Result>;
}
export const DialobFormsContext = React.createContext<DialobFormsContextType>({} as any);

export interface DialobFormsProviderProps {
  tenantId?: string | undefined;
  dialobApiUrl?: string | undefined; 
  fetch?: typeof window.fetch | undefined;
  children: React.ReactNode;
}

const initialData: DashboardState = { forms: [], tags: [], items: [], loadedAt: new Date() };
const windowFetch: typeof window.fetch = (url, options) => window.fetch(url, options);


export const DialobFormsProvider: React.FC<DialobFormsProviderProps> = (props) => {
  
  const backend = React.useMemo(() => new Visitor_RestApi().accept({
    config: { 
      dialobApiUrl: props.dialobApiUrl ?? '', 
      tenantId: props.tenantId
    }, 
    fetch: props.fetch ?? windowFetch
  }), [props.tenantId, props.fetch, props.dialobApiUrl]);


  const { data: state, error, refetch, isPending } = useQuery({
    queryKey: ['dialob-dashboard'],
    queryFn: () => new Visitor_DashboardState().accept(backend),
    initialData
  });

  const contextValue: DialobFormsContextType = React.useMemo(() => {

    // UPLOAD FORM JSON
    async function uploadJsonForm(file: File): Promise<DialobFormsOperationResult> {
      const result = await new Visitor_UploadFormJson().accept(backend, { file, allForms: state.forms });
      await refetch();
      return result;
    }

    // UPLOAD FORM CSV
    async function uploadCsvForm(file: File): Promise<DialobFormsOperationResult> {
      const result = await new Visitor_UploadCsvForm().accept(backend, { file });
      await refetch();
      return result;
    }

    // DOWNLOAD ALL AS JSON
    async function downloadAllForms(forms: DialobRestApi.FormListItem[]) {
      return new Visitor_DownloadAllForms().accept(backend, { forms });
    }

    // CREATE NEW FORM
    async function createForm(input: Visitor_CreateNewForm.Input) {
      const result = await new Visitor_CreateNewForm().accept(backend, { ...input });
      await refetch();
      return result;
    }

    // COPY NEW FORM
    async function copyForm(input: Visitor_CopyForm.Input) {
      const result = await new Visitor_CopyForm().accept(backend, { ...input });
      await refetch();
      return result;
    }

    // DELETE NEW FORM
    async function deleteForm(input: Visitor_DeleteForm.Input) {
      const result = await new Visitor_DeleteForm().accept(backend, { ...input });
      await refetch();
      return result;
    }

    return { uploadJsonForm, downloadAllForms, uploadCsvForm, createForm, copyForm, deleteForm, forms: state.items }
  }, [backend, state]);


  return (<DialobFormsContext.Provider value={contextValue}>
    {props.children}
  </DialobFormsContext.Provider>);
}


export const useDialobForms = () => {
  return React.useContext(DialobFormsContext);
}
