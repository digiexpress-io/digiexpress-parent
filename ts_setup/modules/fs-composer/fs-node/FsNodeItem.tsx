import React from 'react';
import { Box, Collapse, IconButton, List, ListItemIcon } from '@mui/material';

import { FsNode, useFs } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsNodeRoot, StyledListItem, StyledListItemText, getIcon, getIconClassName, getConfigIcons } from './useUtilityClasses';
import { FsIcons } from '../fs-theme';
import { sortChildren } from './fs-node-helpers';

export interface FsNodeProps {
  node: FsNode;
  level: number;
  parentPath?: string;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: FsNode) => void;
  onDoubleClick: (node: FsNode, pathToTopParent: string) => void;
  isDarkTheme: boolean;
  searchTerm: string;
}

export const FsNodeItem: React.FC<FsNodeProps> = ({ node, level, parentPath = '', onToggle, onContextMenu, onDoubleClick, isDarkTheme, searchTerm }) => {
  const children = node.children && node.children.length > 0;
  const configOptions = node.configOptions && node.configOptions.length > 0;
  const classes = useUtilityClasses(isDarkTheme);
  const { isChildError } = useFs();

  const childWithError = children && node.children!.some(child => isChildError(child));
  const showError = node.error || childWithError;

  // Build the full path for this node
  const fullPath = parentPath ? `${parentPath} / ${node.name}` : node.name;

  return (
    <FsNodeRoot className={classes.root} isDarkTheme={isDarkTheme}>
      <StyledListItem
        level={level}
        isDarkTheme={isDarkTheme}
        onClick={() => children && onToggle(node.id)}
        onDoubleClick={() => onDoubleClick(node, fullPath)}
        onContextMenu={(event) => onContextMenu(event, node)}
        error={showError ? true : false}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          {children ? (
            <IconButton size='small'>
              {node.expanded ? <FsIcons.ExpandMore fontSize='small' className={classes.iconExpand} /> : <FsIcons.ChevronRight fontSize='small' className={classes.iconExpand} />}
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
            error={showError ? true : false}
            searchTerm={searchTerm}
          />
          {configOptions && (
            <Box sx={{ marginLeft: 'auto', paddingRight: 1, display: 'flex', gap: 0.5 }}>
              {getConfigIcons(node.configOptions!, classes.iconConfig).map((tooltipIcon) => (
                <Box key={tooltipIcon.key} display='flex' alignItems='center'>
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
              <FsNodeItem
                key={child.id}
                node={child}
                level={level + 1}
                parentPath={fullPath}
                onToggle={onToggle}
                onContextMenu={onContextMenu}
                onDoubleClick={onDoubleClick}
                isDarkTheme={isDarkTheme}
                searchTerm={searchTerm}
              />
            ))}
          </List>
        </Collapse>
      )}
    </FsNodeRoot>
  );
};