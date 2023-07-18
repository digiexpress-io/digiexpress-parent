import React from 'react';
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, IconButton, Typography, DialogTitle as MuiDialogTitle } from '@material-ui/core';

import CloseIcon from '@material-ui/icons/Close';
import TabIcon from '@material-ui/icons/Tab';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      margin: 0,
      padding: theme.spacing(2),
    },
    button: {
      color: theme.palette.grey[500],
    },
    buttons: {
      position: 'absolute',
      right: theme.spacing(1),
      top: theme.spacing(1),
    },
  }),
);



interface DialogTitleProps {
  id: string;
  children: React.ReactNode;
  onClose: () => void;
  onTab?: () => void;
}

const DialogTitle: React.FC<DialogTitleProps> = ({ children, onClose, onTab }) => {
  const classes = useStyles();
  return (
    <MuiDialogTitle className={classes.root}>
      <Typography variant="h6" component="span">{children}</Typography>
      <span className={classes.buttons}>
        {onTab ? (
          <IconButton onClick={onTab} className={classes.button}>
            <TabIcon />
          </IconButton>) : null
        }
        <IconButton onClick={onClose} className={classes.button}>
          <CloseIcon />
        </IconButton>
      </span>
    </MuiDialogTitle>
  );
}

export type { DialogTitleProps };
export { DialogTitle }


