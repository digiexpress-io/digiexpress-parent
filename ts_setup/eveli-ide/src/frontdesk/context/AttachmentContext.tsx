import React, { createContext, PropsWithChildren, useContext } from 'react'
import { SessionRefreshContext } from './SessionRefreshContext'
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl'
import { Attachment, AttachmentUploadResponse } from '../types'

export interface AttachmentContextType {
  loadAttachments:(taskId:number)=>Promise<Attachment[]>
  addAttachment:(taskId:number, file:File)=>Promise<Response|void>;
  downloadAttachmentLink:(taskId:number, filename:string)=>string;
}

export interface AttachmentApiConfig {
  apiBaseUrl: string;
}

const INITIAL_CONFIG = {
  loadAttachments: (taskId:number)=>Promise.resolve([]),
  addAttachment: (taskId:number, file:File)=>{return Promise.resolve()},
  downloadAttachmentLink: (taskId:number, filename:string)=>{return ""}
}
export const AttachmentContext = createContext<AttachmentContextType>(INITIAL_CONFIG);

export const AttachmentContextProvider:React.FC<PropsWithChildren<AttachmentApiConfig>> = ({apiBaseUrl, children}) => {
  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();


  const loadAttachments = (taskId:number) => {
    return session.cFetch(`${apiBaseUrl}/task/${taskId}/files/`)
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
    return session.cFetch(`${apiBaseUrl}/task/${taskId}/files/?filename=${filename}`, 
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
    return `${apiBaseUrl}/task/${taskId}/files/${filename}`
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
