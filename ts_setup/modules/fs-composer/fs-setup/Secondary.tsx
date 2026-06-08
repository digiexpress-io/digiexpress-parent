import React from 'react';
import { FsExplorer } from '../fs-explorer';
import { FsSearchProvider } from '../fs-search';

export const Secondary: React.FC = () => {
  return (
    <FsSearchProvider>
      <FsExplorer />
    </FsSearchProvider>
  );
}