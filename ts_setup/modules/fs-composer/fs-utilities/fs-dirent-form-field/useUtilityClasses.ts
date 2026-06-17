import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../../fs-theme';

const MUI_NAME = 'FsDirentFormField';

interface _OwnerState {
  isDarkMode: boolean;
}

export interface FsDirentFormFieldClasses {
  root: string;
  labelRow: string;
  label: string;
  helperText: string;
}

export type FsDirentFormFieldClassKey = keyof FsDirentFormFieldClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    labelRow: ['labelRow'],
    label: ['label'],
    helperText: ['helperText'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentFormFieldRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: _OwnerState }>(({ theme, ownerState }) => ({
  marginBottom: theme.spacing(1),

  [`& .${MUI_NAME}-labelRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-helperText`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    marginTop: theme.spacing(-1),
  },
}));
