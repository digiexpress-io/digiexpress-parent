import React from 'react';
import { useFsNav } from '@dxs-ts/fs-nav';
import { Box, Collapse, IconButton, List, ListItem, ListItemIcon } from '@mui/material';

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


  function handleClick() {
    if (ownerState.isChildren) {
      ownerState.onToggle(ownerState.dirent.id);
    } else {
      ownerState.openAsset(ownerState.dirent);
    }
  }


  return (<>
    <FsDirentRoot className={classes.root + `${ownerState.showError ? 'error' : ''} ${ownerState.isActive ? 'active' : ''}`} ownerState={ownerState}
      onClick={handleClick}
      onContextMenu={(event) => ownerState.onContextMenu(event, ownerState.dirent)}
      >
      <Box className={classes.explorerDirentContent}>
        {ownerState.isChildren ? (
          <IconButton size='small'>
            {expanded ?
              <FsIcon small icon={FsIcons.ExpandMore} className={classes.iconExpand} /> :
              <FsIcon small icon={FsIcons.ChevronRight} className={classes.iconExpand} />}
          </IconButton>
        ) : (<Box sx={{ paddingLeft: 2 }} />)
        }

        <ListItemIcon className={classes[ownerState.direntIconClassName]}>
          <DirentDecorator dirent={ownerState.dirent}>
            <DirentIcon dirent={ownerState.dirent} />
          </DirentDecorator>
        </ListItemIcon>

        <FsDirentName dirent={ownerState.dirent}
          isDarkTheme={ownerState.isDarkMode}
          error={ownerState.showError ? true : false}
          searchTerm={ownerState.searchTerm}
        />
        <ConfigOptionIcons ownerState={ownerState} />
      </Box>
    </FsDirentRoot>

    {ownerState.isChildren && (
      <Collapse in={expanded} timeout={0}>
        <List disablePadding>
          {ownerState.children.map((child) => (
            <FsDirent
              key={child.id}
              dirent={child}
              level={ownerState.level + 1}
              onToggle={ownerState.onToggle}
              onContextMenu={ownerState.onContextMenu}
              searchTerm={ownerState.searchTerm}
            />
          ))}
        </List>
      </Collapse>
    )}
  </>
  );
};
