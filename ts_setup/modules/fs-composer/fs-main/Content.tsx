import React from 'react';
import { Typography } from '@mui/material';
import { OwnerState } from './useOwnerState';

export interface ContentProps {
  ownerState: OwnerState;
  children: React.ReactNode;
  className: string;
}

export const Content: React.FC<ContentProps> = ({ className, ownerState, children }) => {
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];

  return (
    <div className={className}>
      {activeTab ? (
        <Typography>
          {activeTab.node.name}
          {children}
        </Typography>
      ) : (
        'No asset selected'
      )}

    </div>
  );
};

