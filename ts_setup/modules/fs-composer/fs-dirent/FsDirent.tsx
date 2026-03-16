import React from 'react';
import { Badge, Box, Collapse, IconButton, List, ListItem, ListItemIcon } from '@mui/material';

import { useUtilityClasses, FsDirentRoot } from './useUtilityClasses';
import { FsIcons, FsIcon } from '../fs-theme';
import { FsDirentProps } from './FsDirentProps';
import { useOwnerState } from './useOwnerState';

import { ConfigOptionIcons, FsDirentName, NodeDecorator, NodeIcon } from './Supports';


export const FsDirent: React.FC<FsDirentProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(ownerState.isDarkMode);

  return (
    <FsDirentRoot className={classes.root} ownerState={ownerState}>
      <ListItem
        className={`${classes.explorerNode} ${ownerState.showError ? 'error' : ''}`}
        onClick={() => ownerState.isChildren && ownerState.onToggle(ownerState.node.id)}
        onDoubleClick={() => ownerState.openAsset(ownerState.node, ownerState.fullPath)}
        onContextMenu={(event) => ownerState.onContextMenu(event, ownerState.node)}
      >
        <Box className={classes.explorerNodeContent}>
          {ownerState.isChildren ? (
            <IconButton size='small'>
              {ownerState.node.expanded ? 
                <FsIcon small icon={FsIcons.ExpandMore} className={classes.iconExpand} /> : 
                <FsIcon small icon={FsIcons.ChevronRight} className={classes.iconExpand} />}
            </IconButton>
          ) : (<Box sx={{ width: 21, mr: 0.5 }} />)
          }
          
          <ListItemIcon className={ownerState.nodeIconClassName}>
            <NodeDecorator node={ownerState.node}>
              <NodeIcon node={ownerState.node}/>
            </NodeDecorator>
          </ListItemIcon>

          <FsDirentName node={ownerState.node}
            isDarkTheme={ownerState.isDarkMode}
            error={ownerState.showError ? true : false}
            searchTerm={ownerState.searchTerm}
          />
          <ConfigOptionIcons ownerState={ownerState} />
        </Box>
      </ListItem>
      {ownerState.isChildren && (
        <Collapse in={ownerState.node.expanded} timeout={0}>
          <List component='div' disablePadding>
            {ownerState.children.map((child) => (
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
