import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

export const MUI_NAME = 'FsBreadcrumb';

export interface FsBreadcrumbClasses {
  root: string;
  pathPrefix: string;
  assetName: string;
}

export type FsBreadcrumbClassKey = keyof FsBreadcrumbClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    assetPath: ['assetPath'],
    assetName: ['assetName']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsBreadcrumbRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {

  return {
    height: 30,
    display: 'flex',
    alignItems: 'center',
    width: '100%',
    paddingLeft: theme.spacing(1),
    paddingRight: theme.spacing(1),
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    [`& .${MUI_NAME}-assetPath`]: {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
      ...theme.typography.subtitle2,
      marginLeft: theme.spacing(1)
    },
    [`& .${MUI_NAME}-assetName`]: {
      ...theme.typography.subtitle2,
      fontWeight: 500
    }
  };
});
