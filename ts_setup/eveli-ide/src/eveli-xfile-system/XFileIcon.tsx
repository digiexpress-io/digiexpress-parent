import React from 'react';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import { Box} from '@mui/material';
import { sambucus } from './colors';


export const XFileIcon: React.FC<{}> = ({ }) => {
  return (<Box sx={{
    p: '5px',
    display: 'inline-flex',
    alignItems: 'center'
  }}><DescriptionOutlinedIcon sx={{ color: sambucus }} /></Box>);
};
