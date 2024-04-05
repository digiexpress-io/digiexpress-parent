import React from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import { FormattedMessage } from 'react-intl';


const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}

const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Box display='flex' alignItems='center'>
      <Box width='50%' marginRight={5}>
        <Typography variant='h4'><FormattedMessage id='permissions.principal.create' /></Typography>
      </Box>

      <Box width='50%'>
        TODO: Roles/permissions tabs + right-side content
      </Box>
      <CloseDialogButton onClose={onClose} />
    </Box>
  )

}

export default Header;