import React from 'react';
import { Typography } from '@mui/material';
import { TreeNode } from '../../eveli-tree-api';
import { ReferencesView, PropertiesView, ViewContainer } from './views';

export interface EveliTreeMainRightProps {
  activeNode: TreeNode | undefined;
  selectedView: string | undefined;
}

export const EveliTreeMainRight: React.FC<EveliTreeMainRightProps> = ({ activeNode, selectedView }) => {

  if (!selectedView) {
    return (
      <ViewContainer title='Choose a View'>
        <Typography variant='body2' color='text.secondary'>
          Select an option from the toolbar to view details.
        </Typography>
      </ViewContainer>
    );
  }



  switch (selectedView) {
    case 'references':
      return <ReferencesView node={activeNode} />;
    case 'properties':
      return <PropertiesView node={activeNode} />;
      /*
    case 'configuration':
      return 'Configuration';
    case 'debug':
      return 'Debug';
    case 'preview':
      return 'Preview';
    case 'history':
      return 'History';
    case 'help':
      return 'Help';
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


