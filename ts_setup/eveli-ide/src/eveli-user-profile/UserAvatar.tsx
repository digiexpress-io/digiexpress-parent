import React from 'react';
import { Avatar, Box, Paper, Typography, CircularProgress } from '@mui/material';
import { PrefsApi } from '@/api-prefs';

function stringToColor(string: String) {
  let hash = 0;
  let i;

  /* eslint-disable no-bitwise */
  for (i = 0; i < string.length; i += 1) {
    hash = string.charCodeAt(i) + ((hash << 5) - hash);
  }

  let color = '#';

  for (i = 0; i < 3; i += 1) {
    const value = (hash >> (i * 8)) & 0xff;
    color += `00${value.toString(16)}`.substr(-2);
  }
  /* eslint-enable no-bitwise */
  return color;
}

const UserAvatar: React.FC<{ user: PrefsApi.UserProfile }> = ({ user }) => {

  const firstLetter = user.details.firstName.substring(0, 1).toUpperCase();
  const secondLetter = user.details.lastName.substring(0, 1).toUpperCase();
  const twoLetters = firstLetter + secondLetter;

  if (!user) {
    return <CircularProgress />;
  }

  return (<Box display='flex' justifyContent='start'>
    <Paper variant="outlined" sx={{ p: 0.5, borderRadius: 6, width: 'fit-content' }}>
      <Box display="flex" alignItems="center">
        <Avatar sx={{ backgroundColor: stringToColor(user.details.firstName), mr: 1 }}>{twoLetters}</Avatar>
        <Box display='flex' flexDirection='column'>
          <Typography variant='h4' fontWeight='bolder'>{user.details.firstName + " " + user.details.lastName}</Typography>
          <Typography variant='body1'>@{user.details.username}</Typography>
        </Box>
      </Box>
    </Paper>
  </Box>
  );
};

export { UserAvatar };