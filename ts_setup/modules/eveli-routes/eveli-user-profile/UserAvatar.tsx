import React from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { UserProfileApi } from '@dxs-ts/user-profile';
import { EveliAvatar, EveliUserAvatar, useUtilityClasses } from './useUtilityClasses';

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


const UserAvatar: React.FC<{ user: UserProfileApi.UserProfile }> = ({ user }) => {
  const classes = useUtilityClasses();
  const firstLetter = user.details.firstName.substring(0, 1).toUpperCase();
  const secondLetter = user.details.lastName.substring(0, 1).toUpperCase();
  const twoLetters = firstLetter + secondLetter;

  if (!user) {
    return <CircularProgress />;
  }

  return (
    <EveliUserAvatar className={classes.avatar}>
      <EveliAvatar bgColor={stringToColor(user.details.firstName)}>{twoLetters}</EveliAvatar>
      <Box display='flex' flexDirection='column'>
        <Typography className={classes.avatarUserFirstLastName}>{user.details.firstName + " " + user.details.lastName}</Typography>
        <Typography className={classes.avatarUserName}>@{user.details.username}</Typography>
      </Box>

    </EveliUserAvatar>
  );
};

export { UserAvatar };