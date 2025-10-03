import React from 'react';
import { useSnackbar } from 'notistack';



export const FileUploadButton: React.FC<{ 
  accept: string;
  onChange: (file: File) => Promise<{ success: boolean, message: string }>
  uploadRef: React.MutableRefObject<HTMLInputElement | null>;
}> = ({ accept, uploadRef, onChange: callback }) => {

  const { enqueueSnackbar } = useSnackbar();
  async function handleUpload(event: React.ChangeEvent<HTMLInputElement>) {
    
    const file = event.target.files?.[0];
    if (!file) {
      // prolly should be actual error messsage now this!!!
      const fail = {
        success: false,
        message: 'No file selected',
        error: 'No file selected'
      };
      enqueueSnackbar({ message: fail.message, variant: 'error' });
      return;
    }

    const uploaded = await callback(file);

    // Handle DOM cleanup here - UI concern, not business logic
    if (uploadRef.current) {
      uploadRef.current.value = '';
    }

    if (uploaded.success) {
      enqueueSnackbar({ message: uploaded.message, variant: 'success' });
    } else {
      enqueueSnackbar({ message: uploaded.message, variant: 'error' });
    }
  }

  return (<input type='file' accept={accept} hidden ref={uploadRef} onChange={handleUpload} />)
}

