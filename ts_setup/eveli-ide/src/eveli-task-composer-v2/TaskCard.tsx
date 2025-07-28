import * as React from 'react';
import { Typography, Box, useTheme, Divider, styled, generateUtilityClass, IconButton, alpha, Grid2, Menu, MenuItem, SxProps, Avatar } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import composeClasses from '@mui/utils/composeClasses';

import { flashyCardColorsById, TaskCardStyleDefinition, TaskCardStyleKey, useTaskCardThemeConfig } from './cardThemeConfig';



export interface TaskCardProps {
  id: string;
  title?: string;
  children: React.ReactNode;
  isMenu?: boolean;
  startAdornmentIcon?: React.ReactNode;
  editDialog?: React.ReactNode;
  flashy?: boolean;
  styleVariant?: TaskCardStyleKey;
  onClick?: () => void;
  onDoubleClick?: () => void;
  onReview?: () => void;
  onToggleFlashy?: () => void;
}

interface TaskCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
  style: TaskCardStyleDefinition;
}

interface TitleTextProps {
  children: React.ReactNode
  style: TaskCardStyleDefinition;
}


export const TaskCard: React.FC<TaskCardProps> = (props) => {
  const classes = useUtilityClasses();
  const variant = props.styleVariant ?? 'default';
  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[variant];

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const menuOpen = Boolean(anchorEl);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  function handleMenuClose() {
    setAnchorEl(null);
  };

  function handleFlashyToggle() {
    if (props.onToggleFlashy) {
      props.onToggleFlashy();
    }
    handleMenuClose();
  };

  function onReview() {
    if (props.onReview) {
      props.onReview()
    }
    handleMenuClose()

  }


  return (<>
    {props.editDialog}
    <TaskSectionCard onDoubleClick={props.onDoubleClick} className={classes.dataCard} ownerState={props}> {/* TODO LINK CLICKY CLICK */}
      <Box className={classes.title}>
        {props.startAdornmentIcon}
        <TitleText style={style}>{props.title}</TitleText>
        <Box flexGrow={1} />
        {props.isMenu && <IconButton onClick={handleMenuOpen}><MoreVertIcon color='primary' /></IconButton>}
        <Menu
          anchorEl={anchorEl}
          open={menuOpen}
          onClose={handleMenuClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        >
          <MenuItem onClick={handleFlashyToggle}>
            {props.flashy ? 'Remove Flashy' : 'Make Flashy'}
          </MenuItem>
          {props.id === 'task-form-summary' && (
            <MenuItem onClick={onReview}>
              Form review
            </MenuItem>
          )}

        </Menu>
      </Box>
      <Box className={classes.cardBody}>
        {props.children}
      </Box>
    </TaskSectionCard>
  </>
  );
}

export const TaskCardDataRowText: React.FC<TaskCardDataRowTextProps> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container spacing={theme.spacing(1)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography sx={{ ...style.bodyTypography, fontWeight: 500, whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        <Box sx={{ ...style.bodyTypography, whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {value}
        </Box>
      </Grid2>
    </Grid2>
    <Divider />
  </>
  )
}

export const TaskCardDataRowElement: React.FC<{ label: string, value: React.ReactNode, style: TaskCardStyleDefinition }> = ({ label, value, style }) => {
  const theme = useTheme();

  return (
    <Grid2 container spacing={theme.spacing(1)}>
      <Grid2 size={style.dataRowGridSizes.label}>

        <Typography
          sx={{
            ...style.bodyTypography,
            fontWeight: 500,
            whiteSpace: 'normal',
            wordWrap: 'break-word',
            marginRight: 1
          }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        {value}
      </Grid2>
    </Grid2>
  )
}

export const StartAdornmentIcon: React.FC<{ icon: React.ElementType }> = ({ icon }) => {
  const theme = useTheme();
  const Icon = icon;

  return (
    <Avatar sx={{
      mr: 1,
      border: `1px solid gray`,
      backgroundColor: theme.palette.primary.main
    }}>
      <Icon />
    </Avatar>
  )
}



const MUI_NAME = 'TaskSectionCard';
const TaskSectionCard = styled(Box, {
  name: MUI_NAME,
  slot: 'dataCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.dataCard,
      styles.titleContainer
    ];
  },
})<{ ownerState: { flashy?: boolean; id: string } }>(({ theme, ownerState }) => {
  const { id, flashy } = ownerState;
  const colors = flashyCardColorsById[id] ?? flashyCardColorsById.default;


  const baseStyles: SxProps = {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    paddingLeft: theme.spacing(1),
    paddingRight: theme.spacing(1),

    '& .MuiDivider-root': {
      borderColor: alpha(theme.palette.divider, 0.4)
    },
    ':hover': {
      cursor: 'pointer'
    },
    '& .TaskSectionCard-cardBody': {
      padding: theme.spacing(2),
      border: `1px solid ${theme.palette.divider}`,
      borderRadius: theme.spacing(1),
      flexGrow: 1,
      boxShadow: '-4px 4px 10px rgba(0, 0, 0, 0.08)',
      backgroundColor: theme.palette.background.default
    },
    '& .TaskSectionCard-title': {
      display: 'flex',
      alignItems: 'center',
      height: '3rem',
      paddingLeft: theme.spacing(1),
      color: theme.palette.text.primary,
      border: `1px solid transparent`
    },
  };

  if (flashy) {
    return {
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      border: `2px solid ${colors.flashyBackground}`,
      borderRadius: theme.spacing(1),
      color: theme.palette.text.primary,

      ':hover': {
        cursor: 'pointer'
      },
      '& .MuiSvgIcon-root': {
        color: colors.flashyBackground
      },
      '& .MuiDivider-root': {
        borderColor: `${alpha(colors.flashyBorder, 0.1)}`
      },
      '& .TaskSectionCard-cardBody': {
        padding: theme.spacing(2),
        flexGrow: 1,
        boxShadow: '-4px 4px 10px rgba(0, 0, 0, 0.08)',
        backgroundColor: alpha(colors.flashyBackground, 0.02)
      },
      '& .TaskSectionCard-title': {
        display: 'flex',
        alignItems: 'center',
        height: '3rem',
        paddingLeft: theme.spacing(1),
        color: colors.flashyBackground,
        backgroundColor: alpha(colors.flashyBackground, 0.1),
        '& .MuiAvatar-root': {
          backgroundColor: alpha(colors.flashyBackground, 0.8),
          border: `2px solid ${colors.flashyBackground}`
        },
        '.MuiSvgIcon-root': {
          color: theme.palette.background.default
        }
      }
    };
  }
  return {
    ...baseStyles,
  }
});


export const useUtilityClasses = () => {
  const slots = {
    dataCard: ['dataCard'],
    title: ['title'],
    cardBody: ['cardBody']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const TitleText: React.FC<TitleTextProps> = ({ style, children }) => {

  return (
    <Typography sx={{ ...style.titleTypography, fontWeight: 'bold' }} >
      {children}
    </Typography>
  );
};
