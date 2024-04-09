import React from 'react';
import { TextField, FormControl, FormControlLabel, FormGroup, Switch, Typography, Stack, Divider, styled, SwitchProps } from '@mui/material';

import { useIntl } from 'react-intl';


import Context from 'context';
import { UserProfileDescriptor, ChangeUserDetailsFirstName, ImmutableUserProfileStore } from 'descriptor-access-mgmt';
import { blue_mud2, grey, purple } from 'components-colors';



const FirstName: React.FC<{ init: UserProfileDescriptor }> = ({ init }) => {

  const intl = useIntl();
  const backend = Context.useBackend();
  const [firstName, setFirstName] = React.useState(init.entry.details.firstName);

  function handleFirstNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setFirstName(event.target.value);
  }

  async function handleChange() {
    const command: ChangeUserDetailsFirstName = {
      commandType: 'ChangeUserDetailsFirstName',
      id: init.entry.id,
      firstName
    };
    await new ImmutableUserProfileStore(backend.store).updateUserProfile(init.entry.id, [command]);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'userProfile.frontoffice.firstName' })}
    fullWidth
    value={firstName}
    onChange={handleFirstNameChange}
    onBlur={handleChange}
  />);
}

const LastName: React.FC<{ init: UserProfileDescriptor }> = ({ init }) => {

  const intl = useIntl();
  const [lastName, setLastName] = React.useState(init.entry.details.lastName);

  function handleLastNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setLastName(event.target.value);
  }


  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'userProfile.frontoffice.lastName' })}
    fullWidth
    value={lastName}
    onChange={handleLastNameChange}
    onBlur={() => { }}
  />);
}

const EmailAddress: React.FC<{ init: UserProfileDescriptor }> = ({ init }) => {

  const intl = useIntl();
  const [email, setEmail] = React.useState(init.entry.details.email ?? '');

  function handleEmailChange(event: React.ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
  }

  return (
    <TextField
      InputProps={{ disableUnderline: true }}
      variant="standard"
      placeholder={intl.formatMessage({ id: 'userProfile.frontoffice.email' })}
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
  width: 42,
  height: 26,
  padding: 0,
  margin: 3,
  '& .MuiSwitch-switchBase': {
    padding: 0,
    margin: 2,
    transitionDuration: '300ms',
    '&.Mui-checked': {
      transform: 'translateX(16px)',
      color: '#fff',
      '& + .MuiSwitch-track': {
        backgroundColor: purple,
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
    width: 22,
    height: 22,
  },
  '& .MuiSwitch-track': {
    borderRadius: 26 / 2,
    backgroundColor: theme.palette.mode === 'light' ? blue_mud2 : grey,
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
      <Typography variant='body1' fontWeight='400'>Channel</Typography>
      <FormGroup>
        <FormControlLabel
          control={<StyledSwitch checked={state.a} onChange={handleChange} name="a" />}
          label="Receive email messages for new events"
        />
        <FormControlLabel
          control={<StyledSwitch checked={state.b} onChange={handleChange} name="b" />}
          label="Receive system notifications for new events"
        />
      </FormGroup>
    </FormControl>

    <Divider />
    <FormControl component="fieldset" variant="standard">
      <Typography fontWeight='400'>Notification types</Typography>

      <FormGroup>
        <FormControlLabel
          control={<StyledSwitch checked={state.gilad} onChange={handleChange} name="gilad" />}
          label="When a new task is assigned to me"
        />
        <FormControlLabel
          control={<StyledSwitch checked={state.juliet} onChange={handleChange} name="juliet" />}
          label="When a new comment is assigned to me"
        />
        <FormControlLabel
          control={<StyledSwitch checked={state.jason} onChange={handleChange} name="jason" />}
          label="When a task has become overdue"
        />
        <FormControlLabel
          control={<StyledSwitch checked={state.antoine} onChange={handleChange} name="antoine" />}
          label="When a new message from a customer has arrived"
        />

      </FormGroup>
    </FormControl>
  </Stack>)
}



export { FirstName, LastName, EmailAddress, NotificationSettings };