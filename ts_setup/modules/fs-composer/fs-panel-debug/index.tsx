import React from 'react';
import { Fs } from '@dxs-ts/fs-api';
import { DebugView } from './DebugView';

export const FsPanelDebug: React.FC<{ dirent?: Fs.DirentBase }> = ({ dirent }) => {
  return <DebugView />;
};
