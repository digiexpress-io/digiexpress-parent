import { darken, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";



const MUI_NAME = 'FsPanel';

export interface FsPanelClasses {
  root: string;
  content: string;
  header: string;
  mainSection: string;
  secondarySection: string;
}

export type FsPanelClassKey = keyof FsPanelClasses;

export const useUtilityClasses = () => {
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

export const FsPanelRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: darken(FsColors.light.background, 0.01),
  color: FsColors.light.text,
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