import * as React from 'react';
import { Typography, Box, styled, generateUtilityClass, IconButton, alpha, SxProps, Avatar, Collapse } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import DragHandleIcon from '@mui/icons-material/DragHandle';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import composeClasses from '@mui/utils/composeClasses';

import { TaskCardMenu } from './TaskCardMenu';
import { flashyCardColorsById, TaskCardStyleDefinition, useTaskCardThemeConfig } from './cardThemeConfig';
import { TaskCardId, TaskCardStyleKey } from './CardConfigContext';


export interface TaskCardProps {
  id: TaskCardId;
  title?: string;
  titleNotifier?: string | number;

  children: React.ReactNode;
  altChildren?: React.ReactNode;
  styleVariant?: TaskCardStyleKey;

  isMenu?: boolean;
  isExpanded?: boolean;
  isFlashy?: boolean;
  isAltView?: boolean;

  startAdornmentIcon?: React.ReactNode;
  editDialog?: React.ReactNode;
  onClick?: () => void;
  onDoubleClick?: () => void;
  onReview?: () => void;
  onEdit?: () => void;
  onToggleFlashy?: () => void;
  onToggleAltView?: () => void;
  onToggleExpanded?: () => void;
}


interface TitleTextProps {
  children: React.ReactNode;
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

  const handleEdit = () => {
    if (props.onEdit) {
      props.onEdit();
    }
    handleMenuClose();
  };

  const handleCardExpand = () => {
    if (props.onToggleExpanded) {
      props.onToggleExpanded();
    }
  }

  const cardContent = props.isAltView ? props.altChildren : props.children;


  return (<>
    {props.editDialog}
    <TaskSectionCard onDoubleClick={props.onDoubleClick} className={classes.dataCard} ownerState={props} id={props.id}>

      <Box className={classes.title}>
          {props.startAdornmentIcon}
          <TitleText style={style}>{props.title}</TitleText>
          {props.titleNotifier && <Box className={classes.titleNotifier}>{props.titleNotifier}</Box>}

          <Box flexGrow={1} />
          <IconButton onClick={handleCardExpand}><RotatingExpandIcon expanded={props.isExpanded} /></IconButton>
          {props.isMenu && <IconButton onClick={handleMenuOpen}><MoreVertIcon color='primary' /></IconButton>}
          <Box sx={{ cursor: 'grab', userSelect: 'none', alignSelf: 'center' }}>
            <DragHandleIcon color='primary' />
          </Box>
          <TaskCardMenu cardId={props.id}
            anchorEl={anchorEl}
            open={menuOpen}
            onClose={handleMenuClose}
          flashy={props.isFlashy}
            onToggleFlashy={props.onToggleFlashy}
          altView={props.isAltView}
            onToggleAltView={props.onToggleAltView}
            onReview={props.onReview}
            onEdit={handleEdit} />
        </Box>


        <Collapse in={props.isExpanded} timeout="auto" unmountOnExit >
        <Box className={classes.cardBody}>
          <ExpandableBox isExpanded={props.isExpanded}>
            {cardContent}
          </ExpandableBox>
        </Box>

      </Collapse>
    </TaskSectionCard>
  </>
  );
}



export const ExpandableBox = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isExpanded',
})<{ isExpanded?: boolean }>(({ isExpanded, theme }) => ({
  padding: theme.spacing(2),
  transform: isExpanded ? 'scaleY(1)' : 'scaleY(0.95)',
  transformOrigin: 'top',
  opacity: isExpanded ? 1 : 0,
  transition: 'transform 0.3s ease, opacity 0.3s ease',
}));

export const StartAdornmentIcon: React.FC<{ icon: React.ElementType }> = ({ icon }) => {
  const Icon = icon;

  return (
    <Avatar sx={{
      mr: 1,
      border: `1px solid #c6cad2`,
      backgroundColor: alpha("#70798c", 0.1),
      height: '35px',
      width: '35px'
    }}>
      <Icon sx={{ color: '#6c7689', fontSize: '15pt' }} />
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
})<{ ownerState: TaskCardProps }>(({ theme, ownerState }) => {
  const { id, isFlashy } = ownerState;
  const colors = flashyCardColorsById[id] ?? '#333fff';


  const baseStyles: SxProps = {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    transition: 'transform 0.2s ease, border 0.2s ease',
    boxShadow: '-4px 4px 10px rgba(0, 0, 0, 0.06)',

    '& .MuiDivider-root': {
      borderColor: alpha(theme.palette.divider, 0.4)
    },
    ':hover': {
      cursor: 'pointer'
    },
    '& .TaskSectionCard-title': {
      display: 'flex',
      alignItems: 'center',
      padding: theme.spacing(2),
      backgroundColor: theme.palette.secondary.main,
      border: `1px solid ${theme.palette.divider}`,
      color: theme.palette.text.primary,
    },

    '& .TaskSectionCard-cardBody': {
      backgroundColor: theme.palette.background.default,
      border: `1px solid ${theme.palette.divider}`,
      borderTop: 'none',
    },
    '& .TaskSectionCard-titleNotifier': {
      marginLeft: theme.spacing(1),
      color: theme.palette.primary.main,
      minWidth: '4ch',
      display: 'flex',
      justifyContent: 'center',
      padding: theme.spacing(0.5),
      borderRadius: theme.spacing(1),
      backgroundColor: alpha(theme.palette.primary.main, 0.1)
    },
  };

  if (isFlashy) {
    return {
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      border: `1px solid ${colors.flashyBackground}`,
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
        flexGrow: 1,
        backgroundColor: alpha(colors.flashyBackground, 0.05)
      },
      '& .TaskSectionCard-titleNotifier': {
        marginLeft: theme.spacing(1),
        color: theme.palette.primary.main,
        minWidth: '4ch',
        display: 'flex',
        justifyContent: 'center',
        padding: theme.spacing(0.5),
        borderRadius: theme.spacing(1),
        backgroundColor: alpha(theme.palette.primary.main, 0.1)
      },
      '& .TaskSectionCard-title': {
        display: 'flex',
        alignItems: 'center',
        color: colors.flashyBackground,
        paddingBottom: theme.spacing(2),
        padding: theme.spacing(2),
        backgroundColor: alpha(colors.flashyBackground, 0.15),
        borderBottom: `1px solid ${theme.palette.divider}`,

        '& .MuiAvatar-root': {
          backgroundColor: alpha(colors.flashyBackground, 0.3),
          border: `1px solid ${colors.flashyBackground}`
        },

      }
    };
  }
  return {
    ...baseStyles,
  }
});


const RotatingExpandIcon = styled(ExpandMoreIcon, {
  shouldForwardProp: (prop) => prop !== 'expanded',
})<{ expanded?: boolean }>(({ expanded, theme }) => ({
  transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
  transition: 'transform 0.3s ease',
  color: theme.palette.primary.main
}));

export const useUtilityClasses = () => {
  const slots = {
    dataCard: ['dataCard'],
    title: ['title'],
    titleNotifier: ['titleNotifier'],
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
