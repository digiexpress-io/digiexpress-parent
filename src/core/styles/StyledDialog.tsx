import React from 'react';
import { Dialog, DialogTitle, DialogActions, Divider, Typography, SxProps, useTheme } from '@mui/material';
import { FormattedMessage } from 'react-intl';

interface StyledDialogProps {
  open: boolean,
  onClose: () => void,
  title: string | React.ReactElement,
  sx?: SxProps,
  fullWidth?: boolean
}

const StyledDialog: React.FC<StyledDialogProps> = ({ open, onClose, title, children, fullWidth, sx  }) => {
  const theme = useTheme();  

  var styledTitle: React.ReactElement | null = null; 
  if (typeof title === 'string') {
    styledTitle = <FormattedMessage id={title} />
  } else {
    styledTitle = <>{title}</>
  }
  const baseStyle = { fontWeight: theme.typography.h1.fontWeight };
  const styles = sx ? {...baseStyle, ...sx} : baseStyle;
  return (
    <Dialog open={open} onClose={onClose} fullWidth={fullWidth}>
      <DialogTitle>
        <Typography textAlign='center' sx={styles}>
          {styledTitle}
        </Typography>
      </DialogTitle>
      <Divider />
      <DialogActions sx={{alignSelf: 'center'}}>{children}</DialogActions>
    </Dialog>
  );
}

export { StyledDialog }
export type { StyledDialogProps }