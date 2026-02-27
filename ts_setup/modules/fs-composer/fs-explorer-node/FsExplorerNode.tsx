import React from 'react';
import { Box, Collapse, IconButton, List, ListItemIcon } from '@mui/material';

import { useUtilityClasses, FsNodeRoot, StyledListItem, StyledListItemText, getIcon, getIconClassName, getConfigIcons } from './useUtilityClasses';
import { FsIcons } from '../fs-theme';
import { FsExplorerNodeProps } from './FsExplorerNodeProps';
import { useOwnerState } from './useOwnerState';


export const FsExplorerNode: React.FC<FsExplorerNodeProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(ownerState.isDarkMode);

  return (
    <FsNodeRoot className={classes.root} isDarkTheme={ownerState.isDarkMode}>
      <StyledListItem
        level={ownerState.level}
        isDarkTheme={ownerState.isDarkMode}
        onClick={() => ownerState.children && ownerState.onToggle(ownerState.node.id)}
        onDoubleClick={() => ownerState.openAsset(ownerState.node, ownerState.fullPath)}
        onContextMenu={(event) => ownerState.onContextMenu(event, ownerState.node)}
        error={ownerState.showError ? true : false}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          {ownerState.children ? (
            <IconButton size='small'>
              {ownerState.node.expanded ? <FsIcons.ExpandMore fontSize='small' className={classes.iconExpand} /> : <FsIcons.ChevronRight fontSize='small' className={classes.iconExpand} />}
            </IconButton>
          ) : (
              <Box sx={{ width: 21, mr: 0.5 }} />
          )}
          <ListItemIcon className={getIconClassName(ownerState.node, classes)}>
            {getIcon(ownerState.node)}
          </ListItemIcon>
          <StyledListItemText
            nodeType={ownerState.node.type}
            nodeName={ownerState.node.name}
            description={ownerState.node.description}
            isDarkTheme={ownerState.isDarkMode}
            error={ownerState.showError ? true : false}
            searchTerm={ownerState.searchTerm}
          />
          {ownerState.configOptions && (
            <Box sx={{ marginLeft: 'auto', paddingRight: 1, display: 'flex', gap: 0.5 }}>
              {getConfigIcons(ownerState.node.configOptions!, classes.iconConfig).map((tooltipIcon) => (
                <Box key={tooltipIcon.key} display='flex' alignItems='center'>
                  {tooltipIcon}
                </Box>
              ))}
            </Box>
          )}
        </Box>
      </StyledListItem>
      {ownerState.children && (
        <Collapse in={ownerState.node.expanded} timeout={0}>
          <List component='div' disablePadding>
            {ownerState.sortChildren(ownerState.node.children || []).map((child) => (
              <FsExplorerNode
                key={child.id}
                node={child}
                level={ownerState.level + 1}
                parentPath={ownerState.fullPath}
                onToggle={ownerState.onToggle}
                onContextMenu={ownerState.onContextMenu}
                searchTerm={ownerState.searchTerm}
              />
            ))}
          </List>
        </Collapse>
      )}
    </FsNodeRoot>
  );
};