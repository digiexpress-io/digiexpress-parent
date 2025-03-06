import * as React from 'react';
import { Divider, ListItemText, MenuItem, MenuList, Typography } from '@mui/material';
import { UiDevAppPopoverRoot, useUtilityClasses } from './useUtilityClasses';

export interface LocaleSelectProps {
  anchorEl: HTMLElement | null,
  onClose: () => void,
  open: boolean
}


export const LocaleSelect: React.FC<LocaleSelectProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <UiDevAppPopoverRoot
      open={props.open}
      anchorEl={props.anchorEl}
      onClose={props.onClose}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
    >
      <Typography className={classes.popoverTitle}>Language</Typography>
      <Divider />
      <MenuList dense>
        <MenuItem onClick={props.onClose}>
          <ListItemText>English</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Finnish</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Swedish</ListItemText>
        </MenuItem>
      </MenuList>
    </UiDevAppPopoverRoot>
  );
}
