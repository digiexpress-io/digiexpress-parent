import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Box, alpha, useTheme, styled, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';


const StyledDialogTitle = styled(DialogTitle)(({ theme }) => ({
  color: theme.palette.secondary.contrastText,
  fontWeight: 'bold',
  borderBottom: '1px solid gray'
}));


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
  backgroundColor: string;
  children: React.ReactElement;
}

const StyledDialog: React.FC<StyledDialogProps> = (props) => {
  const theme = useTheme();
  const colors = props.backgroundColor.split(".")
  //@ts-ignore
  const color = theme.palette[colors[0]][colors[1]];


  return (
    <Dialog open={props.open} onClose={props.onClose} fullWidth maxWidth="md" >
      <StyledDialogTitle sx={{ mb: 2, backgroundColor: alpha(color, 0.9) }}>
        <FormattedMessage id={props.title} values={props.titleArgs} /></StyledDialogTitle>
      <DialogContent sx={{ color: "mainContent.dark", fontWeight: '400' }}>{props.children}</DialogContent>
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