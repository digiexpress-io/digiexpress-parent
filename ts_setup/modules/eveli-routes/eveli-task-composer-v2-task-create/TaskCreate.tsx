import React from 'react';
import { Box, Chip, generateUtilityClass, Grid2, styled, TextField, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';
import { EveliDatePicker } from '@dxs-ts/eveli-primitives';


export const TaskCreate: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const createdTodayDate = DateTime.now().setLocale('fi').toLocaleString(DateTime.DATE_SHORT);

  return (
    <StyledTaskCreate className={classes.root}>
      <Typography variant='h1'>{intl.formatMessage({ id: 'task.composer.create', defaultMessage: 'Create new task' })}</Typography>
      <Grid2 container className={classes.infoRow}>
        <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2, xl: 2 }} className={classes.infoRowInternal}>
          <Typography>{intl.formatMessage({ id: 'task.composer.type', defaultMessage: 'Type' })}</Typography>
          <Chip label={intl.formatMessage({ id: 'task.composer.type.normal', defaultMessage: 'Normal' })} color='primary' />
        </Grid2>
        <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2, xl: 2 }} className={classes.infoRowInternal}>
          <Typography>{intl.formatMessage({ id: 'task.composer.source', defaultMessage: 'Source' })}</Typography>
          <Chip label={intl.formatMessage({ id: 'task.composer.source.manual', defaultMessage: 'Manual' })} color='primary' />
        </Grid2>
        <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2, xl: 2 }} className={classes.infoRowInternal}>
          <Typography>{intl.formatMessage({ id: 'task.composer.createdDate', defaultMessage: 'Created' })}</Typography>
          <Typography>{createdTodayDate}</Typography>
        </Grid2>
        <Grid2 size={{ xs: 12, sm: 12, md: 6, lg: 6, xl: 6 }} className={classes.infoRowInternal}>
          <Typography>{intl.formatMessage({ id: 'task.composer.dueDate', defaultMessage: 'Due' })}</Typography>
          <EveliDatePicker
            label={intl.formatMessage({ id: 'taskDialog.dueDate' })}
            fullWidth={true}
            //value={currentState.dueDate}
            //onChange={newDate => setFieldValue('dueDate', newDate)}
            value=''
            onChange={() => { }}
          />
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2, xl: 2 }} className={classes.infoRowInternal}>
          <StyledTextField />
        </Grid2>
      </Grid2>
    </StyledTaskCreate>
  )
}




const MUI_NAME = 'TaskCreate';
const StyledTaskCreate = styled('div', {
  name: MUI_NAME,
  slot: 'TaskCreateView',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {

    '& .TaskCreate-infoRow': {
      display: 'flex',
      justifyContent: 'space-between',
    },
    '& .TaskCreate-infoRowInternal': {
      display: 'flex',
      gap: theme.spacing(1),
      alignItems: 'center',
      flexGrow: 1

    }

  };
})

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: 0,
  '& .MuiOutlinedInput-root': {

  },
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    infoRow: ['infoRow'],
    infoRowInternal: ['infoRowInternal']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}