import React, { createContext, useContext } from 'react'
import { SessionRefreshContext } from './SessionRefreshContext'
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl'
import { Attachment, AttachmentUploadResponse } from '../types'
import { useConfig } from './ConfigContext';

export interface AttachmentContextType {
  loadAttachments:(taskId:number)=>Promise<Attachment[]>
  addAttachment:(taskId:number, file:File)=>Promise<Response|void>;
  downloadAttachmentLink:(taskId:number, filename:string)=>string;
}

const INITIAL_CONFIG = {
  loadAttachments: (_taskId: number) => Promise.resolve([]),
  addAttachment: (_taskId: number, _file: File) => { return Promise.resolve() },
  downloadAttachmentLink: (_taskId: number, _filename: string) => { return "" }
}
export const AttachmentContext = createContext<AttachmentContextType>(INITIAL_CONFIG);

export const AttachmentContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const { serviceUrl } = useConfig();

  const loadAttachments = (taskId:number) => {
    return session.cFetch(`${serviceUrl}rest/api/worker/tasks/${taskId}/files/`)
    .then(response => response.json());
  }

  const handleErrors = (response:Response) => {
    if (!response.ok) {
        throw Error(response.statusText);
    }
    return response;
}
  const addAttachment = (taskId:number, file:File):Promise<Response|void> => {
    const filename = file.name;
    return session.cFetch(`${serviceUrl}rest/api/worker/tasks/${taskId}/files/?filename=${filename}`, 
        {method:'POST', headers: {'Content-Type': file.type || 'application/octet-stream'}})
    .then(response=>handleErrors(response))
    .then(response => response.json())
    .then((uploadResponse:AttachmentUploadResponse)=>{
      return session.cFetch(`${uploadResponse.putRequestUrl}`, {method:'PUT', body:file, headers: {'Content-Type': file.type || 'application/octet-stream'}})
      .then(response=>handleErrors(response))
      .then(response=> {
        enqueueSnackbar(intl.formatMessage({id: 'attachment.uploadOk'}, {fileName:filename}), {variant: 'success'});
        return response;
      })
    })
    .catch(error => {
      enqueueSnackbar(intl.formatMessage({id: 'attachment.uploadFailed'}, {fileName:filename}), {variant: 'error'});
    });

  }

  const downloadAttachmentLink = (taskId:number, filename:string) => {
    return `${serviceUrl}rest/api/worker/tasks/${taskId}/files/${filename}`
  }

  const attachmentContext:AttachmentContextType = {
    loadAttachments: loadAttachments,
    addAttachment: addAttachment,
    downloadAttachmentLink: downloadAttachmentLink
  }

  return (
    <AttachmentContext.Provider value={attachmentContext}>
        {children}
    </AttachmentContext.Provider>
  )
}

export const useAttachmentConfig = () => useContext(AttachmentContext);
