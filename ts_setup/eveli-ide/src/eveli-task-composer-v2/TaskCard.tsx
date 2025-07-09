import * as React from 'react';
import { Typography, Box, useTheme, Divider, styled, generateUtilityClass, IconButton, alpha, Grid2 } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { EditDialog } from './EditDialog';
import composeClasses from '@mui/utils/composeClasses';


export interface TaskCardProps {
  id: string;
  title: string;
  children: React.ReactNode;
  buttonLabel?: string | undefined;
  startAdornmentIcon?: React.ReactNode;
}

export const TaskCard: React.FC<TaskCardProps> = (props) => {
  const theme = useTheme();
  const classes = useUtilityClasses();

  const [open, setOpen] = React.useState(false);
  const handleToggle = () => setOpen((prev) => !prev);

  return (<>
    <EditDialog open={open} onClose={handleToggle} dialogTitle='Edit Dialog' />
    <TaskSectionCard onDoubleClick={handleToggle} className={classes.dataCard}>
      <Box pl={theme.spacing(1)} className={classes.dataCardTitleContainer}>
        {props.startAdornmentIcon}
        <Typography className={classes.dataCardTitle}>
          {props.title}
        </Typography>
        <Box flexGrow={1} />
        {props.buttonLabel && <IconButton onClick={handleToggle}><MoreVertIcon color='primary' /></IconButton>}
      </Box>
      <Divider />
      <Box sx={{ flexGrow: 1, p: theme.spacing(1) }}>
        {props.children}
      </Box>
    </TaskSectionCard>
  </>
  );
}


export const TaskCardDataRowText: React.FC<{ label: string, value: string | string[] | undefined }> = ({ label, value }) => {
  const theme = useTheme();
  return (<>
    <Grid2 container spacing={theme.spacing(1)}>
      <Grid2 size={{ xs: 12, sm: 4, md: 4, lg: 4, xl: 4 }}>
        <Typography variant="subtitle2" fontWeight='bold' sx={{ whiteSpace: 'normal', wordWrap: 'break-word' }}>{label}</Typography>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 8, md: 8, lg: 8, xl: 8 }}>
        <Typography variant="subtitle2">{value}</Typography>
      </Grid2>
    </Grid2>
    <Divider sx={{ borderColor: alpha(theme.palette.divider, 0.4) }} />
  </>

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

export const StartAdornmentIcon = (Icon: React.ElementType) => (
  <Icon fontSize='small' color='primary' sx={{ mr: 1 }} />
);

const MUI_NAME = 'TaskSectionCard';
const TaskSectionCard = styled(Box, {
  name: MUI_NAME,
  slot: 'dataCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.dataCard,
      styles.editCardTitle,
      styles.dataCardTitleContainer
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    transition: 'border-color 200ms ease-in-out',
    border: `1px solid transparent`,

    ':hover': {
      cursor: 'pointer',
      backgroundColor: theme.palette.secondary.main,
      boxShadow: '0 1px 3px rgba(0, 0, 0, 0.08)',
      border: `1px solid ${theme.palette.divider}`,

    },
    '& .TaskSectionCard-dataCardTitle': {
      textAlign: 'left',
      ...theme.typography.body2,
      fontWeight: 'bold',
    },
    '& .TaskSectionCard-dataCardTitleContainer': {
      display: 'flex',
      alignItems: 'center',
      height: '3rem',
      backgroundColor: alpha(theme.palette.divider, 0.2) 
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    dataCard: ['dataCard'],
    dataCardTitle: ['dataCardTitle'],
    dataCardTitleContainer: ['dataCardTitleContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
