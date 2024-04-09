import React from 'react';
import { Avatar as MAvatar } from '@mui/material';
import { useAvatar } from 'descriptor-avatar';




export const AvatarUser: React.FC<{ children: string }> = React.memo(({ children }) => {

  const avatar = useAvatar(children);
  if(!avatar) {
    return null;
  }

  const bgcolor: string = avatar.color;
  return (
    <MAvatar sx={{
      bgcolor,
      width: 24,
      height: 24,
      fontSize: 10 }}>
      {avatar.twoLetterCode}
    </MAvatar>
  );
});
