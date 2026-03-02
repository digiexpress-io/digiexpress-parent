import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { OwnerState } from "./useOwnerState";
import { FsColors } from "../fs-theme";



const MUI_NAME = 'FsReferences';

export interface FsReferencesClasses {
  root: string;
  referenceSection: string;
  referencesContainer: string;
  referenceRow: string;
  referenceLocation: string;
  childrenSection: string;
}

export type FsReferencesClassKey = keyof FsReferencesClasses;


export const useUtilityClasses = () => {
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


export const FsReferencesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  [`& .${MUI_NAME}-referenceSection`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-referencesContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '& > div:nth-of-type(odd)': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-referenceRow`]: {
    display: 'flex',
    flexDirection: 'column',
    padding: theme.spacing(1, 1.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-referenceLocation`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
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