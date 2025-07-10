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
      <TitleContainer ownerState={props}>
        {props.startAdornmentIcon}
        <TitleText ownerState={props}>{props.title}</TitleText>

        <Box flexGrow={1} />
        {props.buttonLabel && <IconButton onClick={props.onClick}><MoreVertIcon color='primary' /></IconButton>}
      </TitleContainer>
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
    },
    ':hover': {
      cursor: 'pointer',
      backgroundColor: theme.palette.secondary.main,
      boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    }
  };

  if (ownerState.flashy) {
    return {
      ...baseStyles,
      backgroundColor: alpha(theme.palette.primary.main, 0.1),
      ':hover': {
        cursor: 'pointer',
        backgroundColor: alpha(theme.palette.primary.main, 0.15),
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
    ...baseStyles
  }
});


export const useUtilityClasses = () => {
  const slots = {
    dataCard: ['dataCard'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const TitleText = styled(Typography)<{ ownerState: TaskCardProps }>(({ theme, ownerState }) => ({
  textAlign: 'left',
  fontWeight: 'bold',
  color: ownerState.flashy ? theme.palette.background.default : 'inherit',
}));

const TitleContainer = styled(Box)<{ ownerState: TaskCardProps }>(({ theme, ownerState }) => ({
  display: 'flex',
  alignItems: 'center',
  height: '3rem',
  paddingLeft: theme.spacing(1),
  backgroundColor: ownerState.flashy ? theme.palette.primary.main : alpha(theme.palette.divider, 0.2),
}));