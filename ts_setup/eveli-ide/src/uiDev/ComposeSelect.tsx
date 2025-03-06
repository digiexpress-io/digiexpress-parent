import * as React from 'react';
import { Divider, ListItemText, MenuItem, MenuList, Typography } from '@mui/material';
import { UiDevAppPopoverRoot, useUtilityClasses } from './useUtilityClasses';

interface ComposeSelectProps {
  anchorEl: HTMLElement | null,
  onClose: () => void,
  open: boolean
}


export const ComposeSelect: React.FC<ComposeSelectProps> = (props) => {
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
      <Typography className={classes.popoverTitle}>New</Typography>
      <Divider />
      <MenuList dense>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Article</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Page</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Service</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Link</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Locale</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Migration</ListItemText>
        </MenuItem>
        <MenuItem onClick={props.onClose}>
          <ListItemText>Template</ListItemText>
        </MenuItem>
      </MenuList>
    </UiDevAppPopoverRoot>
  );
}
