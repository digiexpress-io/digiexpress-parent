import React from 'react';
import { TextField, FormControl, FormControlLabel, FormGroup, Typography, Stack, Box, Button } from '@mui/material';

import { useIntl } from 'react-intl';
import { PrefsApi } from '@/api-prefs';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { StyledNotificationSwitch } from './useUtilityClasses';

function useBackend() {
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {});

  async function updateUserProfile(commands: PrefsApi.UserProfileUpdateCommand<any>[]) {
    return restApi().updateUserProfile(commands)
  }

  return { updateUserProfile }
}


const FirstName: React.FC<{ init: PrefsApi.UserProfile }> = ({ init }) => {
  const intl = useIntl();
  const backend = useBackend();
  const [firstName, setFirstName] = React.useState(init.details.firstName);

  function handleFirstNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setFirstName(event.target.value);
  }

  async function handleChange() {
    const command: PrefsApi.ChangeUserDetailsFirstName = {
      commandType: 'ChangeUserDetailsFirstName',
      id: init.id,
      firstName
    };
    await backend.updateUserProfile([command]);
  }

  return (
    <Box display='flex' alignItems='center' justifyContent='space-between'>
      <TextField variant='outlined' sx={{ width: '70%' }}
        label={intl.formatMessage({ id: 'eveli.userProfile.firstName' })}
        value={firstName}
        onChange={handleFirstNameChange}
      />
      <Button onClick={handleChange}>{intl.formatMessage({ id: 'buttons.apply' })}</Button>
    </Box>

  );
}

const LastName: React.FC<{ init: PrefsApi.UserProfile }> = ({ init }) => {
  const intl = useIntl();
  const backend = useBackend();
  const [lastName, setLastName] = React.useState(init.details.lastName);

  function handleLastNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setLastName(event.target.value);
  }


  async function handleChange() {
    const command: PrefsApi.ChangeUserDetailsLastName = {
      commandType: 'ChangeUserDetailsLastName',
      id: init.id,
      lastName
    };
    await backend.updateUserProfile([command]);
  }


  return (
    <Box display='flex' alignItems='center' justifyContent='space-between'>
      <TextField variant='outlined' sx={{ width: '70%' }}
        label={intl.formatMessage({ id: 'eveli.userProfile.lastName' })}
        value={lastName}
        onChange={handleLastNameChange}
        onBlur={handleChange}
      />
      <Button onClick={handleChange}>{intl.formatMessage({ id: 'buttons.apply' })}</Button>

    </Box>
  );
}

const EmailAddress: React.FC<{ init: PrefsApi.UserProfile }> = ({ init }) => {

  const intl = useIntl();
  const [email, setEmail] = React.useState(init.details.email ?? '');

  function handleEmailChange(event: React.ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
  }

  return (
    <TextField variant="outlined"
      label={intl.formatMessage({ id: 'eveli.userProfile.email' })}
      fullWidth
      value={email}
      onChange={handleEmailChange}
      onBlur={() => { }}
    />
  );
};


const NotificationSettings: React.FC<{}> = () => {

  const [state, setState] = React.useState({
    gilad: true,
    jason: false,
    antoine: true,
    juliet: true,
    a: true,
    b: true
  });

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setState({
      ...state,
      [event.target.name]: event.target.checked,
    });
  };

  return (
    <Stack direction='column' spacing={2}>
    <FormControl component="fieldset" variant="standard">
      <Typography variant='body1' fontWeight='500'>Channel</Typography>
      <Box sx={{ p: 1, alignItems: 'center' }}>
        <FormGroup>
          <FormControlLabel
            control={<StyledNotificationSwitch checked={state.a} onChange={handleChange} name="a" />}
            label={<Typography variant="subtitle2">Receive email messages for new events</Typography>}
          />
          <FormControlLabel
            control={<StyledNotificationSwitch checked={state.b} onChange={handleChange} name="b" />}
            label={<Typography variant="subtitle2">Receive system notifications for new events</Typography>}
          />
        </FormGroup>
      </Box>
    </FormControl>

    <FormControl component="fieldset" variant="standard">
      <Typography fontWeight='500'>Notification types</Typography>

      <Box sx={{ p: 1 }}>
        <FormGroup>
          <FormControlLabel
            control={<StyledNotificationSwitch checked={state.gilad} onChange={handleChange} name="gilad" />}
            label={<Typography variant="subtitle2">When a new task is assigned to me</Typography>}
          />
          <FormControlLabel
            control={<StyledNotificationSwitch checked={state.juliet} onChange={handleChange} name="juliet" />}
            label={<Typography variant="subtitle2">When a new comment is assigned to me</Typography>}
          />
          <FormControlLabel
            control={<StyledNotificationSwitch checked={state.jason} onChange={handleChange} name="jason" />}
            label={<Typography variant="subtitle2">When a task has become overdue</Typography>}
          />
          <FormControlLabel
            control={<StyledNotificationSwitch checked={state.antoine} onChange={handleChange} name="antoine" />}
            label={<Typography variant="subtitle2">When a new message from a customer has arrived</Typography>}
          />

        </FormGroup>
      </Box>
    </FormControl>
  </Stack>)
}



export { FirstName, LastName, EmailAddress, NotificationSettings };