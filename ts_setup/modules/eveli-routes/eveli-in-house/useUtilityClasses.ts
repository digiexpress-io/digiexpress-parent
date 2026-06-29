import { styled, generateUtilityClass, Box, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'EveliInHouseFill';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    content: ['content'],
    spacer: ['spacer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const InHouseFillRoot = styled(Box, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [styles.root, styles.content, styles.spacer];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '90vh',

    '& .EveliInHouseFill-content': {
      padding: theme.spacing(3),
      border: `1px solid ${theme.palette.divider}`,
      borderRadius: theme.spacing(1),
      maxWidth: '50vw',
      boxShadow: `0 4px 20px ${alpha(theme.palette.common.black, 0.18)}`,
    },
    '& .EveliInHouseFill-spacer': {
      marginBottom: theme.spacing(3),
    },
  };
});
