import React from 'react';
import { Dialog } from '@mui/material';

import { StyledFullScreenDialog } from 'components-generic';


const RoleCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  return (
    <StyledFullScreenDialog
      open={open}
      onClose={onClose}
      header={'HEADER'}
      left={<>LEFT</>}
      right={<>RIGHT</>}
      footer={<>FOOTER</>}
    />
  )
}

export default RoleCreateDialog;