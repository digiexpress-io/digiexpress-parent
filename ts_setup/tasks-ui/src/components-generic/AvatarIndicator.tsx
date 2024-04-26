import React from 'react';
import { Box } from '@mui/material';

import { useAvatar } from 'descriptor-avatar';



export const AvatarIndicator: React.FC<{ userId: string }> = React.memo(({userId}) => {

  const avatar = useAvatar(userId);
  return (<Box sx={{ width: 8, height: 40, backgroundColor: avatar?.colorCode }} />);
  
});