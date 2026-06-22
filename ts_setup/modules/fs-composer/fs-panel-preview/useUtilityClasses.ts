import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";


const MUI_NAME = 'FsPanelPreview';

export interface FsPanelPreviewClasses {
  root: string;
  content: string;
  editor: string;
}

export type FsPanelPreviewClassKey = keyof FsPanelPreviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    content: ['content'],
    editor: ['editor'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelPreviewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({

  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),

  [`& .${MUI_NAME}-content`]: {
    ...theme.typography.body2,
    color: FsColors.base.text,
  },

  [`& .${MUI_NAME}-editor`]: {
    height: '500px',
    overflow: 'hidden',
  },
}));
