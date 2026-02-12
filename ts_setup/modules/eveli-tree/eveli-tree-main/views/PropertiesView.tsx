import React from 'react';
import { Typography } from '@mui/material';
import { TreeNode } from '../../../eveli-tree-api';
import { TreeIcons } from '../../tree-theme';
import { ViewContainer } from './ViewContainer';

export interface PropertiesViewProps {
  node: TreeNode | undefined;
}

export const PropertiesView: React.FC<PropertiesViewProps> = ({ node }) => {
  if (!node) {
    return (
      <ViewContainer
        title="Properties"
        icon={<TreeIcons.Settings />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view properties."
      >
        <></>
      </ViewContainer>
    );
  }

  const mainContent = (
    <Typography variant="body2" color="text.secondary">
      Properties view for {node.name} - Coming soon
    </Typography>
  );

  return (
    <ViewContainer
      title={`Properties: ${node.name}`}
      icon={<TreeIcons.Settings />}
      activeNode={true}
    >
      {mainContent}
    </ViewContainer>
  );
}