import React from 'react';
import { TextField, FormControl, FormControlLabel, FormGroup, Switch, Typography, Stack, Divider, styled, SwitchProps, Box } from '@mui/material';

import { useIntl } from 'react-intl';
import { PrefsApi } from '@/api-prefs';
import { useFetch } from '@dxs-ts/eveli-fetch';

function useBackend() {
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {});

  async function updateUserProfile(commands: PrefsApi.UserProfileUpdateCommand<any>[]) {
    return restApi().updateUserProfile('current', commands)
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

  return (<TextField variant='outlined'
    fullWidth
    label={intl.formatMessage({ id: 'eveli.userProfile.firstName' })}
    value={firstName}
    onChange={handleFirstNameChange}
    onBlur={handleChange}
  />);
}

const LastName: React.FC<{ init: PrefsApi.UserProfile }> = ({ init }) => {

  const intl = useIntl();
  const [lastName, setLastName] = React.useState(init.details.lastName);

  function handleLastNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setLastName(event.target.value);
  }


  return (<TextField variant='outlined'

    label={intl.formatMessage({ id: 'eveli.userProfile.lastName' })}
    fullWidth
    value={lastName}
    onChange={handleLastNameChange}
    onBlur={() => { }}
  />);
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


const StyledSwitch = styled((props: SwitchProps) => (
  <Switch focusVisibleClassName=".Mui-focusVisible" disableRipple {...props} />

))(({ theme }) => ({

  width: 29,
  height: 18,
  padding: 0,
  marginRight: theme.spacing(3),

  '& .MuiSwitch-switchBase': {
    padding: 0,
    margin: 1.4,
    transitionDuration: '300ms',
    '&.Mui-checked': {
      transform: 'translateX(11px)',
      color: '#fff',
      '& + .MuiSwitch-track': {
        opacity: 1,
        border: 0,
      },
      '&.Mui-disabled + .MuiSwitch-track': {
        opacity: 0.5,
      },
    },
    '&.Mui-disabled .MuiSwitch-thumb': {
      color:
        theme.palette.mode === 'light'
          ? theme.palette.grey[100]
          : theme.palette.grey[600],
    },
    '&.Mui-disabled + .MuiSwitch-track': {
      opacity: theme.palette.mode === 'light' ? 0.7 : 0.3,
    },
  },
  '& .MuiSwitch-thumb': {
    boxSizing: 'border-box',
    width: 15.4,
    height: 15.4,
  },
  '& .MuiSwitch-track': {
    borderRadius: 9.1,
    opacity: 1,
    transition: theme.transitions.create(['background-color'], {
      duration: 500,
    }),
  },
}));

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

  return (<Stack direction='column' spacing={2}>

    <FormControl component="fieldset" variant="standard">
      <Typography variant='body1' fontWeight='500'>Channel</Typography>
      <Box sx={{ p: 1, alignItems: 'center' }}>
        <FormGroup>
          <FormControlLabel
            control={<StyledSwitch checked={state.a} onChange={handleChange} name="a" />}
            label={<Typography variant="subtitle2">Receive email messages for new events</Typography>}
          />
          <FormControlLabel
            control={<StyledSwitch checked={state.b} onChange={handleChange} name="b" />}
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
            control={<StyledSwitch checked={state.gilad} onChange={handleChange} name="gilad" />}
            label={<Typography variant="subtitle2">When a new task is assigned to me</Typography>}
          />
          <FormControlLabel
            control={<StyledSwitch checked={state.juliet} onChange={handleChange} name="juliet" />}
            label={<Typography variant="subtitle2">When a new comment is assigned to me</Typography>}
          />
          <FormControlLabel
            control={<StyledSwitch checked={state.jason} onChange={handleChange} name="jason" />}
            label={<Typography variant="subtitle2">When a task has become overdue</Typography>}
          />
          <FormControlLabel
            control={<StyledSwitch checked={state.antoine} onChange={handleChange} name="antoine" />}
            label={<Typography variant="subtitle2">When a new message from a customer has arrived</Typography>}
          />

        </FormGroup>
      </Box>
    </FormControl>
  </Stack>)
}



export { FirstName, LastName, EmailAddress, NotificationSettings };