import { generateUtilityClass, styled, alpha, darken } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentCreateArticle';

export interface FsDirentCreateArticleClasses {
  root: string;
  title: string;
  formContainer: string;
  expandToggle: string;
  expandToggleIcon: string;
  expandToggleIconOpen: string;
  optionalFields: string;
  label: string;
  textField: string;
  formControl: string;
  select: string;
  chipContainer: string;
  chip: string;
  menuItem: string;
  menuItemContent: string;
  sectionTitle: string;
  sectionBox: string;
  sectionContent: string;
  buttonContainer: string;
  cancelButton: string;
  saveButton: string;
}

export type FsDirentCreateArticleClassKey = keyof FsDirentCreateArticleClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    expandToggle: ['expandToggle'],
    expandToggleIcon: ['expandToggleIcon'],
    expandToggleIconOpen: ['expandToggleIconOpen'],
    optionalFields: ['optionalFields'],
    label: ['label'],
    textField: ['textField'],
    formControl: ['formControl'],
    select: ['select'],
    chipContainer: ['chipContainer'],
    chip: ['chip'],
    menuItem: ['menuItem'],
    menuItemContent: ['menuItemContent'],
    sectionTitle: ['sectionTitle'],
    sectionBox: ['sectionBox'],
    sectionContent: ['sectionContent'],
    buttonContainer: ['buttonContainer'],
    cancelButton: ['cancelButton'],
    saveButton: ['saveButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentCreateArticleRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    marginBottom: theme.spacing(2),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    padding: '8px 0',
  },

  [`& .${MUI_NAME}-expandToggle`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.direntTypes.dark.form : (darken(FsColors.direntTypes.light.form, 0.1)),
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    fontWeight: 'bold',
    '&:hover': {
      opacity: 0.8,
    },
  },

  [`& .${MUI_NAME}-expandToggleIcon`]: {
    transition: 'transform 200ms ease',
    transform: 'rotate(0deg)',
  },

  [`& .${MUI_NAME}-expandToggleIconOpen`]: {
    transition: 'transform 200ms ease',
    transform: 'rotate(180deg)',
  },

  [`& .${MUI_NAME}-optionalFields`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: '0px !important',
    '& .MuiInputBase-root': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      borderRadius: 0,
      '& fieldset': {
        borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
      },
      '&.Mui-focused fieldset': {
        border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`
      },
    },
    '& .MuiInputBase-input': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
      padding: '8px 12px',
      '&::placeholder': {
        color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
        opacity: 1,
        ...theme.typography.caption,
      },
    },
    '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
      padding: 'unset',
    },
    '& .MuiInputLabel-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
    },
  },

  [`& .${MUI_NAME}-formControl`]: {
    width: '100%',
    marginTop: 'unset !important',
  },

  [`& .${MUI_NAME}-select`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    borderRadius: 0,
    '& .MuiOutlinedInput-notchedOutline': {
      borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      borderRadius: 0,
    },
    '&:hover .MuiOutlinedInput-notchedOutline': {
      borderColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
    },
    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
      border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`
    },
    '& .MuiSvgIcon-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
    '& .MuiSelect-select': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      padding: '8px 12px',
      ...theme.typography.caption,
    },
  },

  [`& .${MUI_NAME}-chipContainer`]: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '4px',
  },

  [`& .${MUI_NAME}-chip`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&.MuiChip-root': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
    '& .MuiChip-label': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
    },
    '& .MuiChip-icon': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
    '& .MuiSvgIcon-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-menuItem`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    },
    '&.Mui-selected': {
      backgroundColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
      '&:hover': {
        backgroundColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.text,
      },
    },
  },

  [`& .${MUI_NAME}-menuItemContent`]: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',

    '& .MuiSvgIcon-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-sectionTitle`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: '8px',
    display: 'block',
  },

  [`& .${MUI_NAME}-sectionBox`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    border: `1px solid ${ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary}`,
    borderRadius: '4px',
    padding: '12px',
    marginTop: '0px !important'
  },

  [`& .${MUI_NAME}-sectionContent`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-buttonContainer`]: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
    justifyContent: 'flex-end',
  },

  [`& .${MUI_NAME}-cancelButton`]: {
    backgroundColor: alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.1),
    color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    border: `1px solid ${alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.3)}`,
    borderRadius: '4px',
    padding: '6px 16px',
    textTransform: 'none',
    fontSize: '12px',
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.2),
    },
  },

  [`& .${MUI_NAME}-saveButton`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    color: ownerState.isDarkMode ? FsColors.light.background : FsColors.light.text,
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary}`,
    borderRadius: '4px',
    padding: '6px 16px',
    textTransform: 'none',
    fontSize: '12px',
    fontWeight: 500,
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.dark.text,
      borderColor: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary,
    },
  },
}));