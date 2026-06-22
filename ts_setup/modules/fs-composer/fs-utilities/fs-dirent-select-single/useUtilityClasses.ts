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
  const dangerColor = FsColors.semantic.danger;
  const borderColor = FsColors.base.border;
  const activeBorderColor = ownerState.showRequiredError ? dangerColor : borderColor;

  return {
    width: '100%',
    marginTop: 'unset !important',

    [`& .${MUI_NAME}-select`]: {
      backgroundColor: FsColors.base.background,
      color: FsColors.base.text,
      borderRadius: 0,
      width: '100%',

      '& .MuiOutlinedInput-notchedOutline': {
        borderColor: activeBorderColor,
        borderRadius: 0,
      },
      '&:hover .MuiOutlinedInput-notchedOutline': {
        borderColor: ownerState.showRequiredError ? dangerColor : FsColors.base.textSecondary,
      },
      '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
        border: `1px solid ${ownerState.showRequiredError ? dangerColor : FsColors.base.text}`,
      },
      '& .MuiSvgIcon-root': {
        color: FsColors.base.text,
      },
      '& .MuiSelect-select': {
        backgroundColor: FsColors.base.background,
        color: FsColors.base.text,
        padding: theme.spacing(1),
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
