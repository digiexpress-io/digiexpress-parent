import React from 'react';
import { Typography, Box, styled, generateUtilityClass, IconButton, alpha, SxProps, Avatar, Collapse, Button, lighten, darken } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import DragHandleIcon from '@mui/icons-material/DragHandle';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import composeClasses from '@mui/utils/composeClasses';

import { TaskCardMenu } from './TaskCardMenu';
import { flashyCardColorsById, TaskCardStyleDefinition, useTaskCardThemeConfig } from './cardThemeConfig';
import { TaskCardId, TaskCardStyleKey } from './CardConfigContext';
import { useIntl } from 'react-intl';


export interface TaskCardProps {
  id: TaskCardId;
  title?: string;
  titleNotifier?: string | number;

  children: React.ReactNode;
  styleVariant?: TaskCardStyleKey;

  isMenu?: boolean;
  isExpanded?: boolean;
  isFlashy?: boolean;

  startAdornmentIcon?: React.ReactNode;
  editDialog?: React.ReactNode;

  showFlashyToggle: boolean;
  showReviewOnMenu: boolean;
  showEditOnMenu: boolean;
  showEditButton: boolean;

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
  const intl = useIntl();

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

  const cardContent = props.children;


  return (<>
    {props.editDialog}
    <TaskSectionCard onDoubleClick={props.onDoubleClick} className={classes.dataCard} ownerState={props} id={props.id}>

      <Box className={classes.title}>
        {props.startAdornmentIcon}
        <TitleText style={style}>{props.title}</TitleText>
        {props.titleNotifier != null && <Box className={classes.titleNotifier}>{props.titleNotifier}</Box>}

        <Box flexGrow={1} />
        {props.showEditButton && <Button variant='text' onClick={handleEdit}>{intl.formatMessage({ id: 'taskCard.title.edit', defaultMessage: 'Edit' })}</Button>}
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
          onToggleAltView={props.onToggleAltView}
          onReview={props.onReview}
          onEdit={handleEdit} 
          showEdit={props.showEditOnMenu}
          showFlashyToggle={props.showFlashyToggle}
          showReview={props.showReviewOnMenu}
          />
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
      padding: theme.spacing(1),
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
      border: `1px solid ${darken(colors.flashyBorder, 0.2)}`,
      color: colors.contrastText,
      boxShadow: `-4px 4px 10px ${alpha(colors.flashyBorder, 0.1)}`,

      ':hover': {
        cursor: 'pointer'
      },
      '& .MuiSvgIcon-root': {
        color: darken(colors.flashyBorder, 0.2),
      },
      '& .MuiButton-root': {
        color: colors.contrastText,
      },
      '& .MuiDivider-root': {
        borderColor: `${alpha(colors.flashyBorder, 0.1)}`
      },

      '& .TaskSectionCard-cardBody': {
        flexGrow: 1,
        borderTop: `1px solid ${colors.flashyBorder}`,
      },
      '& .TaskSectionCard-titleNotifier': {
        marginLeft: theme.spacing(1),
        color: 'white',
        fontWeight: 500,
        minWidth: '4ch',
        display: 'flex',
        justifyContent: 'center',
        padding: theme.spacing(0.5),
        borderRadius: theme.spacing(1),
        border: `1px solid ${colors.flashyBorder}`,
        backgroundColor: alpha(colors.flashyBorder, 0.9)
      },
      '& .TaskSectionCard-title': {
        display: 'flex',
        alignItems: 'center',
        paddingBottom: theme.spacing(2),
        padding: theme.spacing(1),
        backgroundColor: colors.flashyBackground,
        '& .MuiAvatar-root': {
          border: `1px solid ${colors.flashyBorder}`,
          backgroundColor: alpha(colors.flashyBorder, 0.3),
          '& .MuiSvgIcon-root': {
            color: colors.flashyBorder,
          },
        },

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
    titleNotifier: ['titleNotifier'],
    cardBody: ['cardBody']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


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

const ExpandableBox = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isExpanded',
})<{ isExpanded?: boolean }>(({ isExpanded, theme }) => ({
  padding: theme.spacing(2),
  transform: isExpanded ? 'scaleY(1)' : 'scaleY(0.95)',
  transformOrigin: 'top',
  opacity: isExpanded ? 1 : 0,
  transition: 'transform 0.3s ease, opacity 0.3s ease',
}));


const RotatingExpandIcon = styled(ExpandMoreIcon, {
  shouldForwardProp: (prop) => prop !== 'expanded',
})<{ expanded?: boolean }>(({ expanded, theme }) => ({
  transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
  transition: 'transform 0.3s ease',
  color: theme.palette.primary.main
}));



const TitleText: React.FC<TitleTextProps> = ({ style, children }) => {

  return (
    <Typography sx={{ ...style.titleTypography, fontWeight: 'bold' }} >
      {children}
    </Typography>
  );
};
