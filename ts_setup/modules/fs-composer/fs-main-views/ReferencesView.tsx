import React from 'react';
import { Box, Typography, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';


interface ReferencesViewProps {
  node: FsNode | undefined;
}

export const ReferencesView: React.FC<ReferencesViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const { findReferencesToNode } = useFs();
  const classes = useUtilityClasses(isDarkMode);

  if (!node) {
    return (
      <ViewContainer title="References" icon={<FsIcons.Tree />} activeNode={false} noNodeMessage="Select a node from the tree to view references.">
        <></>
      </ViewContainer>
    );
  }

  const references = findReferencesToNode(node);

  const secondaryContent = node.children && node.children.length > 0 ? (
    <div className={classes.childrenSection}>
      <Typography variant="subtitle2">Child References</Typography>
      {node.children.map((child) => (
        <Box key={child.id}>
          <Typography variant="body2">{child.name}</Typography>
          <Typography variant="caption" color="text.secondary">
            Type: {child.type} {child.reference && ' (REF)'}
          </Typography>
        </Box>
      ))}
    </div>
  ) : undefined;

  return (
    <ViewContainer title={`References: ${node.name}`} icon={<FsIcons.Tree />} secondaryChildren={secondaryContent} activeNode={true}>
      <ReferencesViewRoot isDarkMode={isDarkMode} className={classes.root}>
        <div className={classes.referenceSection}>
          {references.length > 0 ? (
            <div className={classes.referencesContainer}>
              {references.map((ref, index) => (
                <div key={index} className={classes.referenceRow}>
                  <Typography className={classes.referenceLocation}>{ref.location}</Typography>
                </div>
              ))}
            </div>
          ) : (
            <Typography variant="body2" color="text.secondary">
              This node does not contain any references.
            </Typography>
          )}
        </div>
      </ReferencesViewRoot>
    </ViewContainer>
  );
};



const MUI_NAME = 'ReferencesView';

export interface ReferencesViewClasses {
  root: string;
  referenceSection: string;
  referencesContainer: string;
  referenceRow: string;
  referenceLocation: string;
  childrenSection: string;
}

export type ReferencesViewClassKey = keyof ReferencesViewClasses;


const useUtilityClasses = (isDarkMode: boolean) => {
  const slots = {
    root: ['root'],
    referenceSection: ['referenceSection'],
    referencesContainer: ['referencesContainer'],
    referenceRow: ['referenceRow'],
    referenceLocation: ['referenceLocation'],
    childrenSection: ['childrenSection'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


const ReferencesViewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  [`& .${MUI_NAME}-referenceSection`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-referencesContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '& > div:nth-of-type(odd)': {
      backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-referenceRow`]: {
    display: 'flex',
    flexDirection: 'column',
    padding: theme.spacing(1, 1.5),
    backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-referenceLocation`]: {
    ...theme.typography.subtitle2,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-childrenSection`]: {
    marginTop: theme.spacing(2),
    '& .MuiTypography-subtitle2': {
      marginBottom: theme.spacing(1),
    },
    '& .MuiBox-root': {
      marginBottom: theme.spacing(1),
    },
  },
}));