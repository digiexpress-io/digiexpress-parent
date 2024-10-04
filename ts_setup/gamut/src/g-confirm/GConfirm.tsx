import React from 'react';
import { useThemeProps, DialogTitle, DialogActions, DialogContent, Button, Typography, Divider } from '@mui/material';
import { useUtilityClasses, GConfirmRoot, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';

export interface GConfirmProps {
  title: string,
  cancelItemName: string | undefined,
  cancelItemMeta?: string | React.ReactNode | undefined,
  content: string,
  cancelButton: string,
  deleteButton: string,
  onClose: () => void;
  open: boolean;

  component?: GOverridableComponent<GConfirmProps>
}


export const GConfirm: React.FC<GConfirmProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { onClose, open, title, cancelItemName, cancelItemMeta, content, cancelButton, deleteButton, component } = props;
  const classes = useUtilityClasses(props);
  const Root = component ?? GConfirmRoot

  return (
    <Root open={open} onClose={onClose} fullWidth ownerState={props} className={classes.root}>
      <DialogTitle>
        <Typography className={classes.title}>{title}</Typography>
      </DialogTitle>
      
      <Divider />

      <DialogContent className={classes.content}>
        <Typography className={classes.cancelItem}>{cancelItemName}</Typography>
        {cancelItemMeta && <Typography className={classes.cancelItemMeta}>{cancelItemMeta}</Typography>}
        <Typography>{content}</Typography>
      </DialogContent>

      <Divider />

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{cancelButton}</Button>
        <Button className={classes.delete} variant='contained' color='error' onClick={onClose}>{deleteButton}</Button>
      </DialogActions>
    </Root>)
}
