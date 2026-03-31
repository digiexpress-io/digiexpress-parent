import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";


const MUI_NAME = 'FsPanelPreview';

export interface FsPanelPreviewClasses {
  root: string;
  content: string;
}

export type FsPanelPreviewClassKey = keyof FsPanelPreviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    content: ['content'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelPreviewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({

  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),

  [`& .${MUI_NAME}-content`]: {
    ...theme.typography.body2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },
}));
