import React from 'react';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { useThemeInfra, GInputAdornmentRoot } from './useThemeInfra';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, IconButton, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { GMarkdown } from '@dxs-ts/gamut-md';



export interface GInputAdornmentClasses {
  root: string;
}
export type GInputAdornmentClassKey = keyof GInputAdornmentClasses;

export interface GInputAdornmentProps {
  id: string;
  title: string | undefined;
  children: string | undefined;
  disabled: boolean;
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
      <IconButton disabled={props.disabled} onClick={handleOpen}><HelpOutlineIcon /></IconButton>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle variant='h2'>{props.title}</DialogTitle>
        <DialogContent>
          <GMarkdown
            overrides={{
              p: ({ children }) => (
                <Typography sx={{ marginBottom: '0px !important' }}>
                  {children}
                </Typography>
              ),
            }}
          >
            {props.children}
          </GMarkdown>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} autoFocus variant='contained'><FormattedMessage id='gamut.buttons.close' /></Button>
        </DialogActions>
      </Dialog>
    </GInputAdornmentRoot>)
}

