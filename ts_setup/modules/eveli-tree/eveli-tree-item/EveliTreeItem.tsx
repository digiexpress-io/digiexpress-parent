import React from 'react';
import { Box, Collapse, IconButton, List, ListItemIcon } from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
} from '@mui/icons-material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemRoot, StyledListItem, StyledListItemText, getIcon, getIconClassName } from './useUtilityClasses';
import { sortChildren } from './eveli-tree-item-helpers';

export interface EveliTreeItemProps {
  node: TreeNode;
  level: number;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: TreeNode) => void;
  isDarkTheme: boolean;
}

export const EveliTreeItem: React.FC<EveliTreeItemProps> = ({ node, level, onToggle, onContextMenu, isDarkTheme }) => {
  const hasChildren = node.children && node.children.length > 0;
  const classes = useUtilityClasses(isDarkTheme);

  return (
    <EveliTreeItemRoot className={classes.root} isDarkTheme={isDarkTheme}>
      <StyledListItem level={level} isDarkTheme={isDarkTheme} onClick={() => hasChildren && onToggle(node.id)} onContextMenu={(event) => onContextMenu(event, node)}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          {hasChildren ? (
            <IconButton size='small'>
              {node.isExpanded ? <ExpandMoreIcon fontSize='small' className={classes.iconExpand} /> : <ChevronRightIcon fontSize='small' className={classes.iconExpand} />}
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
          />
        </Box>
      </StyledListItem>
      {hasChildren && (
        <Collapse in={node.isExpanded} timeout={0}>
          <List component='div' disablePadding>
            {sortChildren(node.children || []).map((child) => (
              <EveliTreeItem
                key={child.id}
                node={child}
                level={level + 1}
                onToggle={onToggle}
                onContextMenu={onContextMenu}
                isDarkTheme={isDarkTheme}
              />
            ))}
          </List>
        </Collapse>
      )}
    </EveliTreeItemRoot>
  );
};