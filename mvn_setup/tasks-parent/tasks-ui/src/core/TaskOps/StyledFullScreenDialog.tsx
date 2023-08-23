import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Box, Stack, Divider, alpha, Theme, styled } from '@mui/material';

function borderColor(theme: Theme) {
  return alpha(
    theme.palette.mainContent.dark
    //theme.palette.uiElements.main
    , 0.3);
}

const StyledDialogTitle = styled(DialogTitle)(({ theme }) => ({
  borderBottom: `1px solid ${borderColor(theme)}`,
  backgroundColor: theme.palette.mainContent.main
}));


const StyledDialogActions = styled(DialogActions)(({ theme }) => ({
  borderTop: `1px solid ${borderColor(theme)}`,
  backgroundColor: theme.palette.mainContent.main
}));


const dialog_padding = 1;
const dialog_height = "100%";


interface StyledFullScreenDialogProps {
  header: React.ReactNode;
  footer?: React.ReactElement;

  onClose: () => void;
  open: boolean;

  left: React.ReactElement;
  right: React.ReactElement;
}


const DialogBlock: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <Box sx={{ width: '50%' }}>
      <Stack spacing={dialog_padding}>
        {children}
      </Stack>
    </Box>
  )
}
const DialogDivider: React.FC<{}> = () => {
  return (
    <Box sx={{ pl: dialog_padding, pr: dialog_padding, height: '100vh' }}>
      <Divider orientation='vertical' sx={{ borderColor: borderColor }} />
    </Box>
  )
}


const StyledFullScreenDialog: React.FC<StyledFullScreenDialogProps> = (props) => {

  return (
    <Dialog open={props.open} onClose={props.onClose} fullScreen sx={{ m: 2 }}>
      <StyledDialogTitle>{props.header}</StyledDialogTitle>
      <DialogContent sx={{ px: dialog_padding, py: 0 }}>
        <Box display='flex' flexDirection='row' height={dialog_height}>
          <DialogBlock>{props.left}</DialogBlock>
          <DialogDivider />
          <DialogBlock>{props.right}</DialogBlock>
        </Box>
      </DialogContent>

      <StyledDialogActions>{props.footer}</StyledDialogActions>
    </Dialog>
  );
}

export type { StyledFullScreenDialogProps }
export { StyledFullScreenDialog }