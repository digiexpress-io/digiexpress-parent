import React from 'react';
import { Typography } from '@mui/material';
import { ReferencesView, ViewContainer } from '../fs-main-views';

import { FsChanges } from '../fs-changes';
import { FsConfigOptions } from '../fs-config-options';
import { FsErrors } from '../fs-errors';
import { FsHelp } from '../fs-help';
import { FsHistory } from '../fs-history';
import { FsProperties } from '../fs-properties';

import { OwnerState } from './useOwnerState';

export interface FsMainRightProps {
  ownerState: OwnerState;
  className: string;
}

export const FsMainRight: React.FC<FsMainRightProps> = ({ ownerState, className }) => {
  const { activeNode, selectedView } = ownerState;

  return (
    <div className={className}>
      {!selectedView ? (
        <ViewContainer title='Choose a View'>
          <Typography>Select an option from the toolbar to view details.</Typography>
        </ViewContainer>
      ) : selectedView === 'changes' ? (
          <FsChanges node={activeNode} />
        ) : (() => {
          switch (selectedView) {
            case 'errors':
              return <FsErrors node={activeNode} />;
            case 'references':
              return <ReferencesView node={activeNode} />;
            case 'properties':
              return <FsProperties node={activeNode} />;
            case 'history':
              return <FsHistory node={activeNode} />;
            case 'help':
              return <FsHelp node={activeNode} />;
            case 'configuration':
              return <FsConfigOptions node={activeNode} />;
            default:
              return (
                <ViewContainer title='View not implemented'>
                  <Typography>
                    The "{selectedView}" view is not yet implemented.
                  </Typography>
                </ViewContainer>
              );
          }
      })()}
    </div>
  );
};


