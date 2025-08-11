import React from 'react';
import { TextField } from '@mui/material';

import { useIntl } from 'react-intl';
import { PrefsApi } from '@dxs-ts/eveli-api';



export const UserProfileLastName: React.FC<{ init: PrefsApi.UserProfile, onChange: (command: PrefsApi.UserProfileUpdateCommand<any>) => void }> = ({ init, onChange }) => {
  const intl = useIntl();
  const [lastName, setLastName] = React.useState(init.details.lastName);

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;

    const command: PrefsApi.ChangeUserDetailsLastName = {
      commandType: 'ChangeUserDetailsLastName',
      id: init.id,
      lastName: value
    };

    onChange(command);
    setLastName(value);
  }


  return (
    <TextField variant='outlined' sx={{ width: '45%' }}
      label={intl.formatMessage({ id: 'eveli.userProfile.lastName' })}
      value={lastName}
      onChange={handleChange}
    />
  );
}
