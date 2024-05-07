import React from 'react';
import { ListItem, Typography, alpha, ListItemProps, styled, ListItemAvatar, Avatar, Box } from '@mui/material';
import WhatshotIcon from '@mui/icons-material/Whatshot';
import PanoramaFishEyeIcon from '@mui/icons-material/PanoramaFishEye';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { cyan, grey_light_2, orange, grey_light } from 'components-colors';

import { useFlyoutMenu } from './FlyoutMenuContext';



const StyledListItem = styled(ListItem)<ListItemProps>(({ theme }) => ({
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: grey_light_2,
    '.MuiAvatar-root': {
      backgroundColor: alpha(cyan, 0.5)
    }
  },
}));

export const FlyoutMenuItem: React.FC<{
  children: React.ReactNode,
  title: string,
  subtitle: string,
  active?: boolean | undefined,
  onClick: () => void;
}> = ({ active, children, title, subtitle, onClick }) => {

  return (
    <StyledListItem sx={{ width: '93%', ml: 2, py: 2, display: 'flex' }} onClick={onClick}>
      <ListItemAvatar>
        <Avatar>{children}</Avatar>
      </ListItemAvatar>
      <Box>
        <Typography variant='h5'>{title}</Typography>
        <Typography variant='caption'>{subtitle}</Typography>
      </Box>

      <Box flexGrow={1} textAlign='right' ml={1}>
        {active ? <CheckCircleIcon sx={{color: cyan}}/> : <PanoramaFishEyeIcon sx={{color: grey_light}} />}
      </Box>
    </StyledListItem>

    
  );
}
