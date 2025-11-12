import { styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'UpsertOneFeedback';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    container: ['container'],
    title: ['title'],
    section: ['section'],
    field: ['field'],
    boldLabel: ['boldLabel'],
    actions: ['actions'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const UpsertOneFeedbackRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root],
})(({ theme }) => ({
  width: '100%',
  display: 'flex',
  flexDirection: 'column',
  padding: theme.spacing(2),

  [`& .${MUI_NAME}-container`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(2),
  },

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.h3,
    fontWeight: theme.typography.fontWeightBold,
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-section`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1),
  },

  [`& .${MUI_NAME}-field`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-boldLabel`]: {
    fontWeight: theme.typography.fontWeightBold,
  },

  [`& .${MUI_NAME}-actions`]: {
    display: 'flex',
    gap: theme.spacing(1),
    marginTop: theme.spacing(2),
  },
}));
