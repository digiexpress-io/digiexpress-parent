import React from 'react';
import { useQuery } from "@tanstack/react-query";

import { DashboardState, DashboardItem } from './types-dashboard';
import { 
  Visitor_DeleteForm, Visitor_CopyForm, Visitor_CreateNewForm, 
  Visitor_DashboardState, Visitor_DownloadAllForms, 
  Visitor_RestApi, Visitor_UploadCsvForm, Visitor_UploadFormJson, 
  Visitor_OpenForm,
  Visitor_LabelAdd,
  Visitor_LabelDelete
} from './visitors';
import { DialobRestApi } from './types-rest-api';


export interface DialobFormsOperationResult {
  success: boolean;
  message: string;
}

export interface DialobFormsContextType {
  forms: DashboardItem[];
  uploadJsonForm: (file: File) => Promise<DialobFormsOperationResult>;
  uploadCsvForm: (file: File) => Promise<DialobFormsOperationResult>;
  downloadAllForms: (forms: DialobRestApi.FormListItem[]) => Promise<{ blob: Blob, fileName: string }>;

  createForm: (props: Visitor_CreateNewForm.Input) => Promise<Visitor_CreateNewForm.Result>;
  copyForm: (props: Visitor_CopyForm.Input) => Promise<Visitor_CopyForm.Result>;
  deleteForm: (props: Visitor_DeleteForm.Input) => Promise<Visitor_DeleteForm.Result>;

  addFormLabel: (props: Visitor_LabelAdd.Input) => Promise<Visitor_LabelAdd.Result>;
  deleteFormLabel: (props: Visitor_LabelDelete.Input) => Promise<Visitor_LabelDelete.Result>;

  openForm: (props: DashboardItem) => void;
}
export const DialobFormsContext = React.createContext<DialobFormsContextType>({} as any);

export interface DialobFormsProviderProps {
  tenantId?: string | undefined;
  dialobApiUrl?: string | undefined; 
  children: React.ReactNode;
  
  fetch?: typeof window.fetch | undefined;

  onOpen?: (props: DashboardItem) => void;
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
      setTimeout(() => refetch(), 250)
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

    // DELETE NEW FORM LABEL
    async function deleteFormLabel(input: Visitor_LabelDelete.Input) {
      const result = await new Visitor_LabelDelete().accept(backend, { ...input });
      await refetch();
      return result;
    }

    // ADD NEW FORM LABEL
    async function addFormLabel(input: Visitor_LabelAdd.Input) {
      const result = await new Visitor_LabelAdd().accept(backend, { ...input });
      await refetch();
      return result;
    }

    // OPEN EXISTING FORM
    function openForm(form: DashboardItem) {
      new Visitor_OpenForm().accept({ 
        form, 
        dialobApiUrl: props.dialobApiUrl,
        tenantId: props.tenantId,
        onOpen: props.onOpen  
      });
    }

    return { 
      addFormLabel, deleteFormLabel,
      uploadJsonForm, downloadAllForms, 
      uploadCsvForm, createForm,
      copyForm, deleteForm, openForm, 
      forms: state.items }
  }, [
    backend, state, 
    props.onOpen, props.dialobApiUrl, props.tenantId
  ]);


  return (<DialobFormsContext.Provider value={contextValue}>
    {props.children}
  </DialobFormsContext.Provider>);
}


export const useDialobForms = () => {
  return React.useContext(DialobFormsContext);
}
