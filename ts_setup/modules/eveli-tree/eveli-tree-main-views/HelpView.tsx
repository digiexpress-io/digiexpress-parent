import React from 'react';
import { TreeNode } from '../../eveli-tree-api';
import { TreeIcons } from '../tree-theme';
import { ViewContainer } from './ViewContainer';

export interface HelpViewProps {
  node: TreeNode | undefined;
}

export const HelpView: React.FC<HelpViewProps> = ({ node }) => {
  return (
    <ViewContainer
      title="Help"
      icon={<TreeIcons.Help />}
      activeNode={true}
    >
      <div>Help view</div>
    </ViewContainer>
  );
};