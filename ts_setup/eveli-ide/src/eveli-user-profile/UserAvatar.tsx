import React from 'react';
import { Avatar, Box, Typography, CircularProgress, useTheme } from '@mui/material';
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
  const theme = useTheme();
  const firstLetter = user.details.firstName.substring(0, 1).toUpperCase();
  const secondLetter = user.details.lastName.substring(0, 1).toUpperCase();
  const twoLetters = firstLetter + secondLetter;

  if (!user) {
    return <CircularProgress />;
  }

  return (
    <Box display="flex" alignItems="center"
      sx={{
        border: `1px solid ${theme.palette.divider}`,
        minWidth: "25%",
        padding: theme.spacing(1),
        borderRadius: theme.spacing(3)
      }}>
        <Avatar sx={{ backgroundColor: stringToColor(user.details.firstName), mr: 1 }}>{twoLetters}</Avatar>
        <Box display='flex' flexDirection='column'>
          <Typography variant='h4' fontWeight='bolder'>{user.details.firstName + " " + user.details.lastName}</Typography>
          <Typography variant='body1'>@{user.details.username}</Typography>
        </Box>
    </Box>
  );
};

export { UserAvatar };