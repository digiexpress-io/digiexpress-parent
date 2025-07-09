import * as React from 'react';
import { Card, CardActions, CardContent, Button, Typography, Box, useTheme, Divider, darken, styled, generateUtilityClass } from '@mui/material';
import { EditDialog } from './EditDialog';
import composeClasses from '@mui/utils/composeClasses';


export interface TaskCardProps {
  id: string;
  title: string;
  children: React.ReactNode;
  buttonLabel?: string | undefined;
}


export const TaskCard: React.FC<TaskCardProps> = (props) => {
  const theme = useTheme();
  const darkPurple = darken(theme.palette.primary.main, 0.5);
  const classes = useUtilityClasses();

  const [open, setOpen] = React.useState(false);
  const handleToggle = () => setOpen((prev) => !prev);

  return (<>
    <EditDialog open={open} onClose={handleToggle} dialogTitle='Edit Dialog' />
    <TaskSectionCard onDoubleClick={handleToggle} className={classes.dataCard}>
      <Box p={theme.spacing(1)}>
        <Typography className={classes.dataCardTitle}>
          {props.title}
        </Typography>
      </Box>
      <CardContent sx={{ flexGrow: 1 }}>
        {props.children}
      </CardContent>
      {props.buttonLabel &&
        <CardActions sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button variant='contained' size='small' onClick={handleToggle}>{props.buttonLabel}</Button>
        </CardActions>}
    </TaskSectionCard>
  </>
  );
}


export const TaskCardDataRowText: React.FC<{ label: string, value: string | undefined }> = ({ label, value }) => {
  return (
    <Box display='flex' justifyContent='space-between'>
      <Typography variant="subtitle2" fontWeight='bold'>{label}</Typography>
      <Typography variant="subtitle2">{value}</Typography>
    </Box>
  )
}

export const TaskCardDataRowElement: React.FC<{ label: string, value: React.ReactNode }> = ({ label, value }) => {
  return (
    <Box display='flex' justifyContent='space-between'>
      <Typography variant="subtitle2" fontWeight='bold'>{label}</Typography>
      {value}
    </Box>
  )
}


const MUI_NAME = 'TaskSectionCard';
const TaskSectionCard = styled(Card, {
  name: MUI_NAME,
  slot: 'dataCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.dataCard,
      styles.editCardTitle
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    padding: theme.spacing(1),
    //width: 350,
    ':hover': {
      cursor: 'pointer'
    },
    '& .TaskSectionCard-dataCardTitle': {
      textAlign: 'left',
      color: darken(theme.palette.primary.main, 0.3),
      ...theme.typography.h4,
      fontWeight: 'bold',
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    dataCard: ['dataCard'],
    dataCardTitle: ['dataCardTitle']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
