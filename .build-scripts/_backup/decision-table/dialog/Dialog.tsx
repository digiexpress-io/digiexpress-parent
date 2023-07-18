import React from 'react';

import { createStyles, makeStyles, withStyles } from '@material-ui/styles';
import {
  Theme,
  Dialog as MuiDialog,
  DialogContent as MuiDialogContent,
  DialogActions as MuiDialogActions,
} from '@material-ui/core';

import { DialogTitle } from './DialogTitle';


const DialogContent = withStyles((theme: Theme) => ({
  root: {
    padding: theme.spacing(2),
  },
}))(MuiDialogContent);

const DialogActions = withStyles((theme: Theme) => ({
  root: {
    margin: 0,
    padding: theme.spacing(1),
  },
}))(MuiDialogActions);

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      width: '100%',
    },
    dialog: {
      height: '500px',
    },
    button: {
      marginRight: theme.spacing(1),
    },
    instructions: {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
  }),
);


interface DialogProps {
  open: boolean;
  title: React.ReactNode;
  content: React.ReactNode;
  actions: React.ReactNode;
  onClose: () => void;
};

const Dialog: React.FC<DialogProps> = ({ open, onClose, title, content, actions }) => {
  const classes = useStyles();
  return (
    <MuiDialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle onClose={onClose}>{title}</DialogTitle>
      <DialogContent className={classes.dialog}>{content}</DialogContent>
      <DialogActions>{actions}</DialogActions>
    </MuiDialog>);
}

export type { DialogProps };
export { Dialog };


