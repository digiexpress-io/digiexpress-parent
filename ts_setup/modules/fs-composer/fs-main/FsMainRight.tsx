import React from 'react';
import { Typography } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { ChangesView, ConfigOptionsView, ErrorsView, ReferencesView, PropertiesView, HistoryView, HelpView, ViewContainer } from '../fs-main-views';

export interface FsMainRightProps {
  activeNode: FsNode | undefined;
  selectedView: string | undefined;
}

export const FsMainRight: React.FC<FsMainRightProps> = ({ activeNode, selectedView }) => {

  if (!selectedView) {
    return (
      <ViewContainer title='Choose a View'>
        <Typography variant='body2' color='text.secondary'>
          Select an option from the toolbar to view details.
        </Typography>
      </ViewContainer>
    );
  }

  if (selectedView === 'changes') {
    return (
      <ChangesView node={activeNode} />
    );
  }



  switch (selectedView) {
    case 'errors':
      return <ErrorsView node={activeNode} />;
    case 'references':
      return <ReferencesView node={activeNode} />;
    case 'properties':
      return <PropertiesView node={activeNode} />;
    case 'history':
      return <HistoryView node={activeNode} />;
    case 'help':
      return <HelpView node={activeNode} />;
    case 'configuration':
      return <ConfigOptionsView node={activeNode} />;
    /*
    case 'debug':
      return 'Debug';
    case 'preview':
      return 'Preview';
      */
    default:
      return (
        <ViewContainer title='View not implemented'>
          <Typography>
            The "{selectedView}" view is not yet implemented.
          </Typography>
        </ViewContainer>
      );
  }
};


