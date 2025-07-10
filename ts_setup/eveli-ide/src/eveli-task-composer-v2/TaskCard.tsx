import * as React from 'react';
import { Typography, Box, useTheme, Divider, styled, generateUtilityClass, IconButton, alpha, Grid2 } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import composeClasses from '@mui/utils/composeClasses';
import { CSSObject } from '@emotion/react';

import { EditDialog } from './EditDialog';
import { TaskCardStyleDefinition, TaskCardStyleKey, useTaskCardStyleConfig } from './TaskCardStyler';



export interface TaskCardProps {
  id: string;
  title?: string;
  children: React.ReactNode;
  buttonLabel?: string | undefined;
  startAdornmentIcon?: React.ReactNode;
  flashy?: boolean;
  styleVariant?: TaskCardStyleKey;
  onClick?: () => void;
}

interface TaskCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
  style: TaskCardStyleDefinition;
}

interface TitleTextProps {
  flashy?: boolean;
  children: React.ReactNode
  style: TaskCardStyleDefinition;
}


export const TaskCard: React.FC<TaskCardProps> = (props) => {
  const theme = useTheme();
  const classes = useUtilityClasses();

  const [open, setOpen] = React.useState(false);
  const handleToggle = () => setOpen((prev) => !prev);

  const variant = props.styleVariant ?? 'default';
  const styleConfig = useTaskCardStyleConfig();
  const style = styleConfig[variant];


  return (<>
    <EditDialog open={open} onClose={handleToggle} dialogTitle='Edit Dialog' /> {/* TODO LINK CLICKY CLICK */}
    <TaskSectionCard onDoubleClick={handleToggle} className={classes.dataCard} ownerState={props} > {/* TODO LINK CLICKY CLICK */}
      <TitleContainer ownerState={props}>
        {props.startAdornmentIcon}
        <TitleText style={style} flashy={props.flashy}>{props.title}</TitleText>
        <Box flexGrow={1} />
        {props.buttonLabel && <IconButton onClick={props.onClick}><MoreVertIcon color='primary' /></IconButton>}
      </TitleContainer>
      <Divider />
      <Typography p={theme.spacing(1)}>
        {props.children}
      </Typography>
    </TaskSectionCard>
  </>
  );
}

export const TaskCardDataRowText: React.FC<TaskCardDataRowTextProps> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container spacing={theme.spacing(1)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography sx={{ ...style.bodyTypography, fontWeight: 'bold', whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        <Typography sx={{ ...style.bodyTypography, whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {value}
        </Typography>
      </Grid2>
    </Grid2>
    <Divider />
  </>
  )
}

// TODO
export const TaskCardDataRowElement: React.FC<{ label: string, value: React.ReactNode }> = ({ label, value }) => {
  return (
    <Box display='flex' justifyContent='space-between'>
      <Typography fontWeight='bold'>{label}</Typography>
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
      borderRadius: theme.spacing(1),
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


const TitleText: React.FC<TitleTextProps> = ({ style, flashy, children }) => {
  const theme = useTheme();

  return (
    <Typography sx={{ ...style.titleTypography, fontWeight: 'bold', color: flashy ? theme.palette.background.default : 'inherit' }} >
      {children}
    </Typography>
  );
};

const TitleContainer = styled(Box)<{ ownerState: TaskCardProps }>(({ theme, ownerState }) => ({
  display: 'flex',
  alignItems: 'center',
  height: '3rem',
  paddingLeft: theme.spacing(1),
  backgroundColor: ownerState.flashy ? theme.palette.primary.main : alpha(theme.palette.divider, 0.2),
}));