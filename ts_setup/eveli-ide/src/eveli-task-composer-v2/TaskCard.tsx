import * as React from 'react';
import { Card, CardActions, CardContent, CardMedia, Button, Typography, Box, useTheme, Divider, darken, styled, generateUtilityClass } from '@mui/material';
import network3 from './network3.jpg';
import { EditDialog } from './EditDialog';
import composeClasses from '@mui/utils/composeClasses';


export interface TaskCardProps {
  id: string;
  title: string;
  children: React.ReactNode;
}


export const TaskCard: React.FC<TaskCardProps> = (props) => {
  const theme = useTheme();
  const darkPurple = darken(theme.palette.primary.main, 0.7);
  const classes = useUtilityClasses();

  const [open, setOpen] = React.useState(false);
  const handleToggle = () => setOpen((prev) => !prev);

  return (<>
    <EditDialog open={open} onClose={handleToggle} dialogTitle='Edit Dialog' />
    <TaskEditSectionCard onDoubleClick={handleToggle} className={classes.editCard}>
      <Box sx={{ position: 'relative', height: 50 }}>
        <CardMedia sx={{ height: 50 }} image={network3} title="Task" />
        <Typography
          sx={{
            position: 'absolute',
            top: 10,
            right: 8,
            fontWeight: 'bold',
            color: theme.palette.background.default,
            textShadow: `
              -4px 0 8px ${darkPurple},
               4px 0 8px ${darkPurple},
               0 0 10px ${darkPurple},
               0 0 20px ${darkPurple}
                `
          }}
        >
          {props.title}
        </Typography>
      </Box>
      <Divider />
      <CardContent  sx={{ flexGrow: 1 }}>
        {props.children}
      </CardContent>
      <CardActions sx={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Button variant='contained' size='small' onClick={handleToggle}>Edit</Button>
      </CardActions>
    </TaskEditSectionCard>
  </>
  );
}


export const TaskCardDataRow: React.FC<{ label: string, value: string | undefined }> = ({ label, value }) => {
  return (
    <Box display='flex' justifyContent='space-between'>
      <Typography variant="subtitle2" fontWeight='bold'>{label}</Typography>
      <Typography variant="subtitle2">{value}</Typography>
    </Box>

  )
}


const MUI_NAME = 'TaskEditSectionCard';
const TaskEditSectionCard = styled(Card, {
  name: MUI_NAME,
  slot: 'editCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.editCard
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    height: 250,
    width: 350,
    boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)',
    transition: 'box-shadow 0.2s ease-in-out',
    ':hover': {
      boxShadow: '0px 6px 20px rgba(0, 0, 0, 0.25)',
      cursor: 'pointer'
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    editCard: ['editCard'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
