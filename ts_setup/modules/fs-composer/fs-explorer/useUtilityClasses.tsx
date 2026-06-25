import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { FsColors } from '../fs-theme';

export const MUI_NAME = 'FsExplorer';

export interface FsExplorerClasses {
  root: string;
  title: string;
  titleText: string;
  icon: string;
  badge: string;
  noSearchResults: string;
}

export type FsExplorerClassKey = keyof FsExplorerClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleText: ['titleText'],
    icon: ['icon'],
    badge: ['badge'],
    noSearchResults: ['noSearchResults']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsExplorerRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => {

  return {
    backgroundColor: theme.palette.secondary.main,
    color: FsColors.base.text,
    flex: 1,
    minHeight: 0,
    fontSize: '13px',
    overflowY: 'auto',
    overflowX: 'hidden',

    [`& .${MUI_NAME}-title`]: {
      borderBottom: `1px solid ${FsColors.base.border}`,
      backgroundColor: FsColors.base.surface,
      padding: theme.spacing(1),
      display: 'flex',
      alignItems: 'center',
      width: '100%'
    },

    [`& .${MUI_NAME}-titleText`]: {
      color: FsColors.base.text,
      ...theme.typography.subtitle2,
      fontWeight: 500
    },

    [`& .${MUI_NAME}-icon`]: {
      size: 'small',
      color: FsColors.base.text,
      [`&.Mui-disabled`]: {
        opacity: 0.35,
        color: FsColors.base.textSecondary,
      },
    },

    [`& .${MUI_NAME}-badge .MuiBadge-badge`]: {
      backgroundColor: FsColors.base.text,
      color: FsColors.base.background,
      height: '10px',
      width: '10px',
      minWidth: '10px',
      borderRadius: '50%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2px',
      right: '2px',
      bottom: '2px',
    },

    [`& .${MUI_NAME}-noSearchResults`]: {
      padding: theme.spacing(2),
      color: FsColors.base.text,
      ...theme.typography.subtitle2,
      fontStyle: 'italic',
      fontWeight: 500,
    },
  };
});
