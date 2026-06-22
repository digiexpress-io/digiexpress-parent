import React from 'react';
import { MenuItem } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsTabMenuProps } from './FsTabMenuProps';
import { useOwnerState } from './useOwnerState';
import { FsTabMenuRoot, useUtilityClasses } from './useUtilityClasses';


export const FsTabMenu: React.FC<FsTabMenuProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const { closeTab, closeAllTabs, closeTabsToTheRight, closeOtherTabs } = useFsNav();

  function handleClose() {
    if (props.tabIndex !== undefined) {
      closeTab(props.tabIndex);
    }
    props.onClose();
  }

  function handleCloseToTheRight() {
    if (props.tabIndex !== undefined) {
      closeTabsToTheRight(props.tabIndex);
    }
    props.onClose();
  }

  function handleCloseOthers() {
    if (props.tabIndex !== undefined) {
      closeOtherTabs(props.tabIndex);
    }
    props.onClose();
  }

  function handleCloseAll() {
    closeAllTabs();
    props.onClose();
  }

  return (
    <FsTabMenuRoot
      className={classes.root}
      open={props.open}
      onClose={props.onClose}
      anchorReference='anchorPosition'
      anchorPosition={props.anchorPosition}
      anchorOrigin={{ vertical: 'top', horizontal: 'left' }}
      transformOrigin={{ vertical: 'top', horizontal: 'left' }}
    >
      <MenuItem className={classes.menuItem} onClick={handleClose}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.close' })}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleCloseToTheRight}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.closeToTheRight' })}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleCloseOthers}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.closeOthers' })}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleCloseAll}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.closeAll' })}
      </MenuItem>
    </FsTabMenuRoot>
  );
};
