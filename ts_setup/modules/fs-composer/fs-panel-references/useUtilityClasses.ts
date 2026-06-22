import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";



const MUI_NAME = 'FsPanelReferences';

export interface FsPanelReferencesClasses {
  root: string;
  referenceSection: string;
  referencesContainer: string;
  referenceRow: string;
  referenceLocation: string;
  childrenSection: string;
}

export type FsPanelReferencesClassKey = keyof FsPanelReferencesClasses;


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


export const FsPanelReferencesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  [`& .${MUI_NAME}-referenceSection`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-referencesContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${FsColors.base.border}`,
    '& > div:nth-of-type(odd)': {
      backgroundColor: FsColors.base.surface,
    },
  },

  [`& .${MUI_NAME}-referenceRow`]: {
    display: 'flex',
    flexDirection: 'column',
    padding: theme.spacing(1, 1.5),
    backgroundColor: FsColors.base.background,
    borderBottom: `1px solid ${FsColors.base.border}`,
    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-referenceLocation`]: {
    ...theme.typography.subtitle2,
    color: FsColors.base.text,
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
