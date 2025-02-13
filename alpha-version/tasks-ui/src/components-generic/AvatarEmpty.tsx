import React from 'react';
import { Avatar } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';


export const AvatarEmpty: React.FC = React.memo(() => {
  return (
    <Avatar sx={{ width: 24, height: 24, fontSize: 10 }}>
      <PersonAddIcon sx={{ fontSize: 15 }} />
    </Avatar>
  );
});
