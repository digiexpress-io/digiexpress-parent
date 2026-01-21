import React from 'react';
import { Typography, Box, styled, generateUtilityClass, IconButton, alpha, SxProps, Avatar, Collapse, Button, lighten, darken } from '@mui/material';
import { MoreVert as MoreVertIcon } from '@mui/icons-material';
import { DragHandle as DragHandleIcon } from '@mui/icons-material';
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';

import { flashyCockpitCardColorsById, CockpitCardStyleDefinition, useCockpitCardThemeConfig } from './cockpitCardThemeConfig';
import { CockpitCardId, CockpitCardStyleKey } from './CockpitCardConfigContext';
import { useIntl } from 'react-intl';

export interface CockpitCardProps {
  id: CockpitCardId;
  title?: string;
  titleNotifier?: string | number | React.ReactNode;

  children: React.ReactNode;
  styleVariant?: CockpitCardStyleKey;

  isMenu?: boolean;
  isExpanded?: boolean;

  startAdornmentIcon?: React.ReactNode;
  editDialog?: React.ReactNode;

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
  style: CockpitCardStyleDefinition;
}

export const CockpitCard: React.FC<CockpitCardProps> = (props) => {
  const classes = useUtilityClasses();
  const variant = props.styleVariant ?? 'default';
  const styleConfig = useCockpitCardThemeConfig();
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
    <CockpitSectionCard className={classes.dataCard} ownerState={props} id={props.id}>

      <Box className={classes.title}>
        <Box display='flex' flexGrow={1} alignItems='center' onClick={props.onToggleExpanded}>
          {props.startAdornmentIcon}
          <TitleText style={style}>{props.title}</TitleText>
          {props.titleNotifier != null && <Box className={classes.titleNotifier}>{props.titleNotifier}</Box>}
        </Box>

        {props.showEditButton && <Button variant='text' onClick={handleEdit}>{intl.formatMessage({ id: 'cockpit.edit' })}</Button>}
        <IconButton onClick={handleCardExpand}><RotatingExpandIcon expanded={props.isExpanded} /></IconButton>
        {props.isMenu && <IconButton onClick={handleMenuOpen}><MoreVertIcon color='primary' /></IconButton>}
        <Box sx={{ cursor: 'grab', userSelect: 'none', alignSelf: 'center' }}>
          <DragHandleIcon color='primary' />
        </Box>
      </Box>

      <Collapse in={props.isExpanded} timeout="auto" unmountOnExit>
        <Box className={classes.cardBody}>
          <ExpandableBox isExpanded={props.isExpanded} onDoubleClick={props.onDoubleClick}>
            {cardContent}
          </ExpandableBox>
        </Box>
      </Collapse>
    </CockpitSectionCard>
  </>
  );
}

const MUI_NAME = 'CockpitSectionCard';
const CockpitSectionCard = styled(Box, {
  name: MUI_NAME,
  slot: 'dataCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.dataCard,
      styles.titleContainer
    ];
  },
})<{ ownerState: CockpitCardProps }>(({ theme, ownerState }) => {
  const { id } = ownerState;
  const colors = flashyCockpitCardColorsById[id] ?? '#333fff';

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
    '& .CockpitSectionCard-title': {
      display: 'flex',
      alignItems: 'center',
      padding: theme.spacing(1),
      backgroundColor: theme.palette.secondary.main,
      border: `1px solid ${theme.palette.divider}`,
      color: theme.palette.text.primary,
    },

    '& .CockpitSectionCard-cardBody': {
      backgroundColor: theme.palette.background.default,
      border: `1px solid ${theme.palette.divider}`,
      borderTop: 'none',
    },
    '& .CockpitSectionCard-titleNotifier': {
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