import React from 'react';
import { TextField, FormControl, FormControlLabel, FormGroup, Switch, Typography, Stack, Divider } from '@mui/material';

import { useIntl } from 'react-intl';


const FirstName: React.FC<{}> = () => {

  const intl = useIntl();
  const [title, setTitle] = React.useState('');

  function handleFirstNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }


  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'userProfile.frontoffice.firstName' })}
    fullWidth
    value={title}
    onChange={handleFirstNameChange}
    onBlur={() => { }}
  />);
}

const LastName: React.FC<{}> = () => {

  const intl = useIntl();
  const [title, setTitle] = React.useState('');

  function handleLastNameChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }


  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'userProfile.frontoffice.lastName' })}
    fullWidth
    value={title}
    onChange={handleLastNameChange}
    onBlur={() => { }}
  />);
}

const EmailAddress: React.FC<{}> = () => {

  const intl = useIntl();
  const [title, setTitle] = React.useState('');

  function handleEmailChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'userProfile.frontoffice.email' })}
    fullWidth
    value={title}
    onChange={handleEmailChange}
    onBlur={() => { }}
  />);
}

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

  return (<Stack direction='column' spacing={1}>

    <FormControl component="fieldset" variant="standard">
      <Typography variant='body1'>Select notification channel</Typography>
      <FormGroup>
        <FormControlLabel
          control={
            <Switch checked={state.a} onChange={handleChange} name="a" />
          }
          label="Receive email messages for new events"
        />
        <FormControlLabel
          control={
            <Switch checked={state.b} onChange={handleChange} name="b" />
          }
          label="Receive system notifications for new events"
        />
      </FormGroup>
    </FormControl>

    <Divider />
    <FormControl component="fieldset" variant="standard">
      <Typography>Fine tune your notification types</Typography>

      <FormGroup>
        <FormControlLabel
          control={
            <Switch checked={state.gilad} onChange={handleChange} name="gilad" />
          }
          label="When a new task is assigned to me"
        />
        <FormControlLabel
          control={
            <Switch checked={state.juliet} onChange={handleChange} name="juliet" />
          }
          label="When a new comment is assigned to me"
        />
        <FormControlLabel
          control={
            <Switch checked={state.jason} onChange={handleChange} name="jason" />
          }
          label="When a task has become overdue"
        />
        <FormControlLabel
          control={
            <Switch checked={state.antoine} onChange={handleChange} name="antoine" />
          }
          label="When a new message from a customer has arrived"
        />

      </FormGroup>
    </FormControl>
  </Stack>)
}



export { FirstName, LastName, EmailAddress, NotificationSettings };