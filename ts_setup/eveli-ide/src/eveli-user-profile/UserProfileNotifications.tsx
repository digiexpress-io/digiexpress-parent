import React from 'react';
import { FormControl, FormControlLabel, FormGroup, Typography, Stack, Box } from '@mui/material';
import { StyledNotificationSwitch } from './useUtilityClasses';





export const UserProfileNotifications: React.FC<{}> = () => {

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
              label={<Typography>Receive email messages for new events</Typography>}
            />
            <FormControlLabel
              control={<StyledNotificationSwitch checked={state.b} onChange={handleChange} name="b" />}
              label={<Typography>Receive system notifications for new events</Typography>}
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
              label={<Typography>When a new task is assigned to me</Typography>}
            />
            <FormControlLabel
              control={<StyledNotificationSwitch checked={state.juliet} onChange={handleChange} name="juliet" />}
              label={<Typography>When a new comment is assigned to me</Typography>}
            />
            <FormControlLabel
              control={<StyledNotificationSwitch checked={state.jason} onChange={handleChange} name="jason" />}
              label={<Typography>When a task has become overdue</Typography>}
            />
            <FormControlLabel
              control={<StyledNotificationSwitch checked={state.antoine} onChange={handleChange} name="antoine" />}
              label={<Typography>When a new message from a customer has arrived</Typography>}
            />

          </FormGroup>
        </Box>
      </FormControl>
    </Stack>)
}



