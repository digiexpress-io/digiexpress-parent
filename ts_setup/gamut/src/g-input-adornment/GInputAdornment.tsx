import React from 'react';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { useThemeInfra, GInputAdornmentRoot } from './useThemeInfra';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, IconButton } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { GMarkdown } from '../g-md';



export interface GInputAdornmentClasses {
  root: string;
}
export type GInputAdornmentClassKey = keyof GInputAdornmentClasses;

export interface GInputAdornmentProps {
  id: string;
  title: string | undefined;
  children: string | undefined;
  component?: React.ElementType<GInputAdornmentProps>;
}

export const GInputAdornment: React.FC<GInputAdornmentProps> = (initProps) => {
  const { classes, ownerState, props } = useThemeInfra(initProps);
  const [open, setOpen] = React.useState(false); 

  if(!props.children) {
    return null;
  }

  function handleOpen() {
    setOpen(true);
  }

  function handleClose() {
    setOpen(false);
  }


  return (
    <GInputAdornmentRoot ownerState={ownerState} as={ownerState.component} className={classes.root}>
      <IconButton onClick={handleOpen}><HelpOutlineIcon color='disabled'/></IconButton>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>{props.title}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            <GMarkdown>{props.children}</GMarkdown>
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} autoFocus variant='contained'><FormattedMessage id='gamut.buttons.close'/></Button>
        </DialogActions>
      </Dialog>
    </GInputAdornmentRoot>)
}

