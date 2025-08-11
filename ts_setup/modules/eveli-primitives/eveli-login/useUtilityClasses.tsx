import { styled, generateUtilityClass, Button } from '@mui/material';
import { EveliLoginProps } from './EveliLogin';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'EveliLogin'

export interface EveliLoginClasses {
  root: string;
}
export type EveliLoginClassKey = keyof EveliLoginClasses;

export const EveliLoginRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {

  };
});

export const useUtilityClasses = (ownerState: EveliLoginProps) => {
  const slots = { root: ['root'], logout: ['logout'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliLogoutButton = styled(Button, {
  name: MUI_NAME,
  slot: 'logout',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})<{}>(({ theme }) => {

  return {
    justifyContent: 'left',
    alignItems: "flex-start",
    marginTop: theme.spacing(0.5),
    borderRadius: theme.spacing(3),
    paddingLeft: theme.spacing(2),
    border: `1px solid ${theme.palette.secondary.main}`,
    ...theme.typography.body1,
    color: theme.palette.text.secondary,
    width: '100%',
    ':hover': {
      backgroundColor: theme.palette.secondary.dark,
      border: `1px solid ${theme.palette.secondary.main}`,
    }
  }
})

