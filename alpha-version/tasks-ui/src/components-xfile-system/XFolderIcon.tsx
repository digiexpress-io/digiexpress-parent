import React from 'react';
import FolderIcon from '@mui/icons-material/Folder';
import { Box } from '@mui/material';
import { orange } from 'components-colors';


export const XFolderIcon: React.FC<{}> = ({ }) => {
  return (<Box sx={{
    p: '5px',
    display: 'inline-flex',
    alignItems: 'center'
  }}><FolderIcon sx={{ color: orange }} /></Box>);
};
