import React from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';

export interface FsPropertiesFlowTaskProps {
  direntId: string;
}

export const FsPropertiesFlowTask: React.FC<FsPropertiesFlowTaskProps> = ({ direntId }) => {
  const { getDirent } = useFsDirent();
  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'FLOW_TASK') {
    return undefined;
  }

  return <></>;
};
