import React from 'react';
import { Box, Collapse, IconButton, List, ListItem, ListItemIcon } from '@mui/material';

import { useFsNav } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsDirentRoot } from './useUtilityClasses';
import { FsIcons, FsIcon } from '../fs-theme';
import { FsDirentProps } from './FsDirentProps';
import { useOwnerState } from './useOwnerState';

import { ConfigOptionIcons, FsDirentName, DirentDecorator, DirentIcon } from './Supports';


export const FsDirent: React.FC<FsDirentProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(ownerState.isDarkMode);

  const { isExpanded } = useFsNav();
  const expanded = isExpanded(props.dirent.id) ?? false;
  const clickTimerRef = React.useRef<number | undefined>(undefined);

  function handleClick() {
    if (!ownerState.isChildren) {
      return;
    }
    if (clickTimerRef.current) {
      clearTimeout(clickTimerRef.current);
    }
    clickTimerRef.current = window.setTimeout(() => {
      ownerState.onToggle(ownerState.dirent.id);
    }, 250);
  }

  function handleDoubleClick() {
    if (clickTimerRef.current) {
      clearTimeout(clickTimerRef.current);
    }
    ownerState.openAsset(ownerState.dirent, ownerState.fullPath);
  }

  return (
    <FsDirentRoot className={classes.root} ownerState={ownerState}>
      <ListItem
        className={`${classes.explorerDirent} ${ownerState.showError ? 'error' : ''} ${ownerState.isActive ? 'active' : ''}`}
        onClick={handleClick}
        onDoubleClick={handleDoubleClick}
        onContextMenu={(event) => ownerState.onContextMenu(event, ownerState.dirent)}
      >
        <Box className={classes.explorerDirentContent}>
          {ownerState.isChildren ? (
            <IconButton size='small'>
              {expanded ?
                <FsIcon small icon={FsIcons.ExpandMore} className={classes.iconExpand} /> :
                <FsIcon small icon={FsIcons.ChevronRight} className={classes.iconExpand} />}
            </IconButton>
          ) : (<Box sx={{ width: 21, mr: 0.5 }} />)
          }
          
          <ListItemIcon className={classes[ownerState.direntIconClassName]}>
            <DirentDecorator dirent={ownerState.dirent}>
              <DirentIcon dirent={ownerState.dirent}/>
            </DirentDecorator>
          </ListItemIcon>

          <FsDirentName dirent={ownerState.dirent}
            isDarkTheme={ownerState.isDarkMode}
            error={ownerState.showError ? true : false}
            searchTerm={ownerState.searchTerm}
          />
          <ConfigOptionIcons ownerState={ownerState} />
        </Box>
      </ListItem>
      {ownerState.isChildren && (
        <Collapse in={expanded} timeout={0}>
          <List component='div' disablePadding>
            {ownerState.children.map((child) => (
              <FsDirent
                key={child.id}
                dirent={child}
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
