import React from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';

export interface FsPropertiesFlowProps {
  direntId: string;
}

export const FsPropertiesFlow: React.FC<FsPropertiesFlowProps> = ({ direntId }) => {
  const { getDirent } = useFsDirent();
  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'FLOW') {
    return undefined;
  }

  return <></>;
};
