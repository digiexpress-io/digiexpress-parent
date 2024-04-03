import React from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import { FormattedMessage } from 'react-intl';


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Box display='flex' alignItems='center'>
      <Typography variant='h4'><FormattedMessage id='permissions.permission.create' /></Typography>
      <Box flexGrow={1} />
      <IconButton onClick={onClose}>
        <CloseIcon />
      </IconButton>
    </Box>
  )
}

export { Header }