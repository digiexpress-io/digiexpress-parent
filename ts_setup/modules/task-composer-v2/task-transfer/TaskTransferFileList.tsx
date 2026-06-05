import React from 'react';
import { Chip } from '@mui/material';


export interface TaskTransferFileListProps {
  files?: string | string[];
}

export const TaskTransferFileList: React.FC<TaskTransferFileListProps> = ({ files }) => {
  if (!files) {
    return null;
  }

  if (Array.isArray(files)) {
    return (
      <>
        {files.map(file =><Chip label={file} />)}
      </>
    )
  }
  return <Chip label={files} />
}