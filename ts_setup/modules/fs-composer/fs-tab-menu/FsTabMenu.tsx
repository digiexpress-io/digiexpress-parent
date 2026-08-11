import React from 'react';
import { MenuItem } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsTabMenuProps } from './FsTabMenuProps';
import { useOwnerState } from './useOwnerState';
import { FsTabMenuRoot, useUtilityClasses } from './useUtilityClasses';


export const FsTabMenu: React.FC<FsTabMenuProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

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
      <MenuItem className={classes.menuItem} onClick={ownerState.onClose}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.close' })}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={ownerState.onCloseToTheRight}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.closeToTheRight' })}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={ownerState.onCloseOthers}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.closeOthers' })}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={ownerState.onCloseAll}>
        {intl.formatMessage({ id: 'fs.tabMenu.menuItem.closeAll' })}
      </MenuItem>
    </FsTabMenuRoot>
  );
};
