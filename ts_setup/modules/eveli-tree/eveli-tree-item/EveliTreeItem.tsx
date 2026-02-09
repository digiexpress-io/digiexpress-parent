import React from 'react';
import { Box, Collapse, IconButton, List, ListItemIcon } from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
} from '@mui/icons-material';

import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemRoot, StyledListItem, StyledListItemText, getIcon, getIconClassName, getConfigIcons } from './useUtilityClasses';
import { sortChildren } from './eveli-tree-item-helpers';


export interface EveliTreeItemProps {
  node: TreeNode;
  level: number;
  parentPath?: string;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: TreeNode) => void;
  onDoubleClick: (node: TreeNode, pathToTopParent: string) => void;
  isDarkTheme: boolean;
}

export const EveliTreeItem: React.FC<EveliTreeItemProps> = ({ node, level, parentPath = '', onToggle, onContextMenu, onDoubleClick, isDarkTheme }) => {
  const children = node.children && node.children.length > 0;
  const configOptions = node.configOptions && node.configOptions.length > 0;
  const classes = useUtilityClasses(isDarkTheme);

  // Build the full path for this node
  const fullPath = parentPath ? `${parentPath} / ${node.name}` : node.name;

  return (
    <EveliTreeItemRoot className={classes.root} isDarkTheme={isDarkTheme}>
      <StyledListItem
        level={level}
        isDarkTheme={isDarkTheme}
        onClick={() => children && onToggle(node.id)}
        onDoubleClick={() => onDoubleClick(node, fullPath)}
        onContextMenu={(event) => onContextMenu(event, node)}
        error={node.error}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          {children ? (
            <IconButton size='small'>
              {node.expanded ? <ExpandMoreIcon fontSize='small' className={classes.iconExpand} /> : <ChevronRightIcon fontSize='small' className={classes.iconExpand} />}
            </IconButton>
          ) : (
              <Box sx={{ width: 21, mr: 0.5 }} />
          )}
          <ListItemIcon className={getIconClassName(node, classes)}>
            {getIcon(node)}
          </ListItemIcon>
          <StyledListItemText
            nodeType={node.type}
            nodeName={node.name}
            description={node.description}
            isDarkTheme={isDarkTheme}
            error={node.error}
          />
          {configOptions && (
            <Box sx={{ marginLeft: 'auto', paddingRight: 1, display: 'flex', gap: 0.5 }}>
              {getConfigIcons(node.configOptions!, classes.iconConfig).map((tooltipIcon, index) => (
                <Box key={tooltipIcon.key || index}>
                  {tooltipIcon}
                </Box>
              ))}
            </Box>
          )}
        </Box>
      </StyledListItem>
      {children && (
        <Collapse in={node.expanded} timeout={0}>
          <List component='div' disablePadding>
            {sortChildren(node.children || []).map((child) => (
              <EveliTreeItem
                key={child.id}
                node={child}
                level={level + 1}
                parentPath={fullPath}
                onToggle={onToggle}
                onContextMenu={onContextMenu}
                onDoubleClick={onDoubleClick}
                isDarkTheme={isDarkTheme}
              />
            ))}
          </List>
        </Collapse>
      )}
    </EveliTreeItemRoot>
  );
};