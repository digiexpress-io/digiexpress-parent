import React from 'react';
import { Avatar as MAvatar, Box, Typography } from '@mui/material';
import { useAvatar } from 'descriptor-avatar';






export const AvatarUser: React.FC<{ children: string, fullname?: boolean }> = React.memo(({ children, fullname }) => {
  const avatar = useAvatar(children);
  if (!avatar) {
    return null;
  }

  const bgcolor: string = avatar.colorCode;
  const letters = (<MAvatar sx={{ bgcolor, width: 24, height: 24, fontSize: 10 }}>{avatar.letterCode}</MAvatar>);

  if (fullname) {
    return (<Box display='flex' alignItems='center' sx={{ cursor: 'pointer' }}>
      {letters}
      <Box pl={1}><Typography>{avatar.displayName}</Typography></Box>
    </Box>);
  }
  return letters;
});
