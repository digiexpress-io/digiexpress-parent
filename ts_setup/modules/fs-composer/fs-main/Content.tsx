import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { OwnerState } from './useOwnerState';

export interface ContentProps {
  ownerState: OwnerState;
  children: React.ReactNode;
  className: string;
}

export const Content: React.FC<ContentProps> = ({ className, ownerState, children }) => {
  const intl = useIntl();
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];

  return (
    <div className={className}>
      {activeTab ? (
        <Typography>
          {activeTab.node.name}
          {children}
        </Typography>
      ) : (
        intl.formatMessage({ id: 'fs.main.message.noAssetSelected' })
      )}

    </div>
  );
};

