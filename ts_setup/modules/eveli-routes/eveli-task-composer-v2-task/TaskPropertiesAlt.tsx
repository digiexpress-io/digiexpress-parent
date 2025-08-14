import React from 'react';
import { Button, Chip, generateUtilityClass, Grid2, TextField, Typography, styled, TextFieldProps, Box } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import { useTaskDashboard } from '../eveli-task-composer-v2';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card'


function formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
};


export const TaskPropertiesAlt: React.FC<{ style: TaskCardStyleDefinition, onReview(): void; }> = ({ style, onReview }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { task } = useTaskDashboard();

  const { keyWords } = task;
  const isProtected = keyWords?.includes('Protected');
  const isManual = keyWords?.includes('Manual');



  return (
    <StyledTaskPropertiesAlt className={classes.taskAltCard} style={style} container spacing={1}>
      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12 }} className={classes.row}>
        <Box className={classes.cell}>
          <Typography>{intl.formatMessage({ id: 'taskDialog.category' })}</Typography>
          <Chip color={isProtected ? 'error' : 'primary'}
            label={isProtected ? intl.formatMessage({ id: 'task.keywords.protected', defaultMessage: 'Protected' }) : intl.formatMessage({ id: 'task.keywords.normal', defaultMessage: 'Normal' })}
          />
        </Box>

        <Box className={classes.cell}>
          <Typography>
            {intl.formatMessage({ id: 'task.created' })}
            {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
            {formatAnyDateShort(task.created)}
          </Typography>
        </Box>

        <Box className={classes.cell}>
          <Typography>{intl.formatMessage({ id: 'taskDialog.source', defaultMessage: 'Source' })}</Typography>
          <Chip color='primary'
            label={isManual ? intl.formatMessage({ id: 'task.keywords.internal', defaultMessage: 'Internal' }) : intl.formatMessage({ id: 'task.keywords.customerCreated', defaultMessage: 'Customer-created' })}
          />
        </Box>

        <Box className={classes.cell}>
          <Typography>
            {intl.formatMessage({ id: 'task.dueDate' })}
            {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
            {formatAnyDateShort(task.dueDate)}
          </Typography>
        </Box>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12 }} className={classes.row}>
        <StyledTextField fullWidth value={task.clientIdentificator} label={intl.formatMessage({ id: 'taskcard.body.customerName', defaultMessage: 'Customer name' })} />
        <StyledTextField fullWidth value={task.subject} label={intl.formatMessage({ id: 'taskcard.body.subject', defaultMessage: 'Subject' })} />
        <Button variant='contained' onClick={onReview}>{intl.formatMessage({ id: 'taskcard.button.viewForm', defaultMessage: 'View form' })}</Button>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12 }} className={classes.row}>
        <StyledTextField fullWidth value={task.additionalInfo} label={intl.formatMessage({ id: 'taskcard.body.additionalInfo', defaultMessage: 'Additional info' })} />
      </Grid2>
    </StyledTaskPropertiesAlt>
  )
}



const MUI_NAME = 'TaskPropertiesAlt';
const StyledTaskPropertiesAlt = styled(Grid2, {
  name: MUI_NAME,
  slot: 'taskAltCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.taskAlt,
    ];
  },
})<{ style: TaskCardStyleDefinition }>(({ theme, style }) => {

  return {
    gap: theme.spacing(1),
    '& .TaskPropertiesAlt-row': {
      display: 'flex',
      gap: theme.spacing(1),
      flexWrap: 'wrap',
      justifyContent: 'space-between'
    },
    '& .TaskPropertiesAlt-cell': {
      display: "flex",
      gap: theme.spacing(1),
      alignItems: 'center'
    },

    '& .MuiButton-root': {
      padding: theme.spacing(2),

    },

    '& .MuiTypography-root': {
      ...style.bodyTypography
    }
  }
});


export const useUtilityClasses = () => {
  const slots = {
    taskAltCard: ['taskAltCard'],
    row: ['row'],
    cell: ['cell']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const StyledTextField = styled((props: TextFieldProps) => (
  <TextField {...props}
    slotProps={{
      input: {
        readOnly: true,
        style: { pointerEvents: 'none' },
      }
    }}
  />
))(({ theme }) => ({
  cursor: 'default',
  flex: 1,

}));

