import * as React from 'react';
import { Typography, Box, useTheme, Divider, styled, generateUtilityClass, IconButton, alpha, Grid2 } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { EditDialog } from './EditDialog';
import composeClasses from '@mui/utils/composeClasses';
import { CSSObject } from '@emotion/react';


export interface TaskCardProps {
  id: string;
  title?: string;
  children: React.ReactNode;
  buttonLabel?: string | undefined;
  startAdornmentIcon?: React.ReactNode;
  flashy?: boolean;
  onClick?: () => void;
}

export const TaskCard: React.FC<TaskCardProps> = (props) => {
  const theme = useTheme();
  const classes = useUtilityClasses();

  const [open, setOpen] = React.useState(false);
  const handleToggle = () => setOpen((prev) => !prev);

  return (<>
    <EditDialog open={open} onClose={handleToggle} dialogTitle='Edit Dialog' /> {/* TODO LINK CLICKY CLICK */}
    <TaskSectionCard onDoubleClick={handleToggle} className={classes.dataCard} ownerState={props}> {/* TODO LINK CLICKY CLICK */}
      <Box className={classes.dataCardTitleContainer}>
        {props.startAdornmentIcon}
        <Typography className={classes.dataCardTitle}>
          {props.title}
        </Typography>
        <Box flexGrow={1} />
        {props.buttonLabel && <IconButton onClick={props.onClick}><MoreVertIcon color='primary' /></IconButton>}
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
    <Divider />
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
})<{ ownerState: TaskCardProps }>(({ theme, ownerState }) => {

  const baseStyles: CSSObject = {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    transition: 'border-color 200ms ease-in-out',
    border: `1px solid ${theme.palette.divider}`,
    borderRadius: theme.spacing(1),
    '& .MuiDivider-root': {
      borderColor: alpha(theme.palette.divider, 0.4)
    }
  };

  if (ownerState.flashy) {
    return {
      ...baseStyles,
      backgroundColor: alpha(theme.palette.primary.main, 0.1),

      ':hover': {
        cursor: 'pointer',
        backgroundColor: alpha(theme.palette.primary.main, 0.15),
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
      },

      '& .TaskSectionCard-dataCardTitle': {
        textAlign: 'left',
        fontWeight: 'bold',
        color: theme.palette.background.default,
      },
      '& .TaskSectionCard-dataCardTitleContainer': {
        display: 'flex',
        alignItems: 'center',
        height: '3rem',
        backgroundColor: theme.palette.primary.main,
        paddingLeft: theme.spacing(1),
      },
      '& .MuiSvgIcon-root': {
        color: theme.palette.background.paper
      },
      '& .MuiDivider-root': {
        borderColor: `${alpha(theme.palette.primary.main, 0.2)}`
      }
    };
  }

  return {
    ...baseStyles,
    ':hover': {
      cursor: 'pointer',
      backgroundColor: theme.palette.secondary.main,
      boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    },
    '& .TaskSectionCard-dataCardTitle': {
      textAlign: 'left',
      fontWeight: 'bold',
      ...theme.typography.body2,
    },
    '& .TaskSectionCard-dataCardTitleContainer': {
      display: 'flex',
      alignItems: 'center',
      height: '3rem',
      backgroundColor: alpha(theme.palette.divider, 0.2),
      paddingLeft: theme.spacing(1),
    },
  };
});


export const useUtilityClasses = () => {
  const slots = {
    dataCard: ['dataCard'],
    dataCardTitle: ['dataCardTitle'],
    dataCardTitleContainer: ['dataCardTitleContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
