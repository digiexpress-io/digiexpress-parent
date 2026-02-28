import React from 'react';
import { Box, Typography, darken, lighten, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';

export interface ViewContainerProps {
  title: string;
  icon?: React.ReactNode;
  children: React.ReactNode;
  secondaryChildren?: React.ReactNode;
  noNodeMessage?: string;
  activeNode?: boolean;
}

const MUI_NAME = 'ViewContainer';

export interface ViewContainerClasses {
  root: string;
  content: string;
  header: string;
  mainSection: string;
  secondarySection: string;
}

export type ViewContainerClassKey = keyof ViewContainerClasses;

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

const ViewContainerRoot = styled('div', {
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

export const ViewContainer: React.FC<ViewContainerProps> = ({
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
    <ViewContainerRoot isDarkMode={isDarkMode} className={classes.root}>
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
    </ViewContainerRoot>
  );
};

