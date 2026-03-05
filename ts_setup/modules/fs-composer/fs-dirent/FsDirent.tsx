import React from 'react';
import { Box, Collapse, IconButton, List, ListItem, ListItemIcon } from '@mui/material';

import { useUtilityClasses, FsDirentRoot, getIcon, getIconClassName, getConfigIcons, FsDirentName } from './useUtilityClasses';
import { FsIcons } from '../fs-theme';
import { FsDirentProps } from './FsDirentProps';
import { useOwnerState } from './useOwnerState';
import { FsNode } from '@dxs-ts/fs-api';


export const FsDirent: React.FC<FsDirentProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(ownerState.isDarkMode);

  return (
    <FsDirentRoot className={classes.root} ownerState={ownerState}>
      <ListItem
        className={`${classes.explorerNode} ${ownerState.showError ? 'error' : ''}`}
        onClick={() => ownerState.children && ownerState.onToggle(ownerState.node.id)}
        onDoubleClick={() => ownerState.openAsset(ownerState.node, ownerState.fullPath)}
        onContextMenu={(event) => ownerState.onContextMenu(event, ownerState.node)}
      >
        <Box className={classes.explorerNodeContent}>
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
          <FsDirentName node={ownerState.node}
            isDarkTheme={ownerState.isDarkMode}
            error={ownerState.showError ? true : false}
            searchTerm={ownerState.searchTerm}
          />
          {ownerState.configOptions && (<ConfigOptionIcons iconConfig={classes.iconConfig} node={props.node} />)}
        </Box>
      </ListItem>
      {ownerState.children && (
        <Collapse in={ownerState.node.expanded} timeout={0}>
          <List component='div' disablePadding>
            {ownerState.sortChildren(ownerState.node.children || []).map((child) => (
              <FsDirent
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
    </FsDirentRoot>
  );
};

interface ConfigOptionIconsProps {
  node: FsNode;
  iconConfig: string;
}

const ConfigOptionIcons: React.FC<ConfigOptionIconsProps> = ({ node, iconConfig }) => {

  return (
    <Box sx={{ marginLeft: 'auto', paddingRight: 1, display: 'flex', gap: 0.5 }}>
      {getConfigIcons(node.configOptions!, iconConfig).map((tooltipIcon) => (
        <Box key={tooltipIcon.key} display='flex' alignItems='center'>
          {tooltipIcon}
        </Box>
      ))}
    </Box>
  )
}