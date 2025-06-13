import React from 'react';
import { TextField } from '@mui/material';

import { useIntl } from 'react-intl';
import { PrefsApi } from '@/api-prefs';





export const UserProfileFirstName: React.FC<{ init: PrefsApi.UserProfile, onChange: (command: PrefsApi.UserProfileUpdateCommand<any>) => void }> = ({ init, onChange }) => {
  const intl = useIntl();
  const [firstName, setFirstName] = React.useState(init.details.firstName);

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;

    const command: PrefsApi.ChangeUserDetailsFirstName = {
      commandType: 'ChangeUserDetailsFirstName',
      id: init.id,
      firstName: value
    };

    onChange(command);
    setFirstName(value);
  }


  return (
    <TextField variant='outlined' sx={{ width: '45%' }}
      label={intl.formatMessage({ id: 'eveli.userProfile.firstName' })}
      value={firstName}
      onChange={handleChange}
    />
  );
}
