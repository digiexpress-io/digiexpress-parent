import React from 'react';
import { Box, Typography, darken, lighten, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { FsMainContentProps } from './FsMainContentProps';



const MUI_NAME = 'FsMainContent';

export interface FsMainContentClasses {
  root: string;
  content: string;
  header: string;
  mainSection: string;
  secondarySection: string;
}

export type FsMainContentClassKey = keyof FsMainContentClasses;

const useUtilityClasses = (isDarkMode: boolean) => {
  const slots = {
    root: ['root'],
    content: ['content'],
    header: ['header'],
    mainSection: ['mainSection'],
    secondarySection: ['secondarySection'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const FsMainContentRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? lighten(FsColors.dark.background, 0.03) : darken(FsColors.light.background, 0.01),
  color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
  overflow: 'auto',

  [`& .${MUI_NAME}-content`]: {
    display: 'flex',
    flexDirection: 'column',
    padding: theme.spacing(1),
  },

  [`& .${MUI_NAME}-header`]: {
    display: 'flex',
    marginBottom: theme.spacing(1.25),
    '& .MuiBox-root': {
      marginRight: theme.spacing(1),
    },
  },

  [`& .${MUI_NAME}-mainSection`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-secondarySection`]: {
    marginTop: theme.spacing(2),
  },
}));

export const FsMainContent: React.FC<FsMainContentProps> = ({
  title,
  icon,
  children,
  secondaryChildren,
  noNodeMessage,
  activeNode = true
}) => {
  const { isDarkMode } = useFs();
  const classes = useUtilityClasses(isDarkMode);

  return (
    <FsMainContentRoot isDarkMode={isDarkMode} className={classes.root}>
      <div className={classes.content}>
        <div className={classes.header}>
          {icon && <Box>{icon}</Box>}
          <Typography variant="body1" fontWeight={500}>{title}</Typography>
        </div>

        <div className={classes.mainSection}>
          {activeNode ? children : (
            <Typography variant="body2" color="text.secondary">
              {noNodeMessage || 'Select a node from the tree to view details.'}
            </Typography>
          )}
        </div>

        {activeNode && secondaryChildren && (
          <div className={classes.secondarySection}>
            {secondaryChildren}
          </div>
        )}
      </div>
    </FsMainContentRoot>
  );
};

