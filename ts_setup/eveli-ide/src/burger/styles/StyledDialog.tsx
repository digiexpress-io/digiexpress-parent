import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Box, alpha, useTheme, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';




interface StyledDialogProps {
  title: string;
  titleArgs?: {};
  onClose: () => void;
  submit?: {
    title: string;
    disabled: boolean;
    onClick: () => void;
  };
  actions?: React.ReactElement;
  open: boolean;
  children: React.ReactElement;
}

const StyledDialog: React.FC<StyledDialogProps> = (props) => {

  return (
    <Dialog open={props.open} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id={props.title} values={props.titleArgs} /></DialogTitle>
      <DialogContent>{props.children}</DialogContent>
      <DialogActions>
        <Box display="inline-flex">
        
          {props.actions}
          <Button variant='text' sx={{ mr: 1 }} onClick={props.onClose}>
            <FormattedMessage id='button.cancel'/>
          </Button>
          {props.submit ? <Button onClick={props.submit.onClick} disabled={props.submit.disabled}><FormattedMessage id={props.submit.title}/></Button> : undefined }
        
        </Box>
      </DialogActions>
    </Dialog>
  );
}

export type { StyledDialogProps }
export { StyledDialog }