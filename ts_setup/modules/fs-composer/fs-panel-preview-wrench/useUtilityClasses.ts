import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { OwnerState } from "./useOwnerState";


const MUI_NAME = 'FsPanelPreviewWrench';

export interface FsPanelPreviewWrenchClasses {
  root: string;
  editor: string;
}

export type FsPanelPreviewWrenchClassKey = keyof FsPanelPreviewWrenchClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    editor: ['editor'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelPreviewWrenchRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme }) => ({

  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),

  [`& .${MUI_NAME}-editor`]: {
    height: 'calc(100vh - 250px)',
    overflow: 'hidden',
  },
}));
