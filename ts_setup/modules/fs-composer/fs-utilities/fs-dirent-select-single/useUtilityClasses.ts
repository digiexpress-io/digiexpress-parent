import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentSelectSingle';

export interface FsDirentSelectSingleClasses {
  root: string;
  select: string;
  requiredMessage: string;
}

export type FsDirentSelectSingleClassKey = keyof FsDirentSelectSingleClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    select: ['select'],
    requiredMessage: ['requiredMessage'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentSelectSingleRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  const dangerColor = ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight;
  const borderColor = ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border;
  const activeBorderColor = ownerState.showRequiredError ? dangerColor : borderColor;

  return {
    width: '100%',
    marginTop: 'unset !important',

    [`& .${MUI_NAME}-select`]: {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      borderRadius: 0,
      width: '100%',

      '& .MuiOutlinedInput-notchedOutline': {
        borderColor: activeBorderColor,
        borderRadius: 0,
      },
      '&:hover .MuiOutlinedInput-notchedOutline': {
        borderColor: ownerState.showRequiredError ? dangerColor : (ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary),
      },
      '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
        border: `1px solid ${ownerState.showRequiredError ? dangerColor : (ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text)}`,
      },
      '& .MuiSvgIcon-root': {
        color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      },
      '& .MuiSelect-select': {
        backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
        color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
        padding: theme.spacing(1.5),
        ...theme.typography.subtitle1,
      },
    },

    [`& .${MUI_NAME}-requiredMessage`]: {
      ...theme.typography.subtitle2,
      display: ownerState.showRequiredError ? 'block' : 'none',
      color: dangerColor,
      marginTop: '3px',
      marginLeft: 0,
      '&.MuiTypography-root': {
        color: dangerColor,
      },
    },
  };
});
