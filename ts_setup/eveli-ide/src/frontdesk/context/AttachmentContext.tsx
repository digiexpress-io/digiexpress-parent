import React, { createContext, useContext } from 'react'
import { Attachment } from '../types'
import { useFetch } from '@dxs-ts/eveli-fetch';

export interface AttachmentContextType {
  loadAttachments:(taskId: string) => Promise<Attachment[]>
  addAttachment:(taskId: string, file: File) => Promise<Response|void>;
  downloadAttachmentLink:(taskId: string, filename: string) => string;
}

const INITIAL_CONFIG = {
  loadAttachments: (_taskId: string) => Promise.resolve([]),
  addAttachment: (_taskId: string, _file: File) => { return Promise.resolve() },
  downloadAttachmentLink: (_taskId: string, _filename: string) => { return "" }
}
export const AttachmentContext = createContext<AttachmentContextType>(INITIAL_CONFIG);

export const AttachmentContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {  
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
  const { downloadAttachmentLink } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.GET', {});
  const { addAttachment } = useFetch('worker/rest/api/tasks/$taskId/files.POST', {});

  const attachmentContext: AttachmentContextType = {
    loadAttachments, addAttachment, downloadAttachmentLink
  }
  return (
    <AttachmentContext.Provider value={attachmentContext}>
      {children}
    </AttachmentContext.Provider>
  )
}

export const useAttachmentConfig = () => useContext(AttachmentContext);
