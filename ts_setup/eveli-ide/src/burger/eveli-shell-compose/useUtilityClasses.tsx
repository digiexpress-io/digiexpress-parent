
import { generateUtilityClass, Popover, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'EveliShellCompose';


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliShellComposeRoot = styled(Popover, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title
    ];
  },
})(({ theme }) => {
  return {
    '& .MuiPaper-root': {
      minWidth: 200
    },
    '& .EveliShellCompose-title': {
      fontWeight: 'bold',
      padding: theme.spacing(2)
    }
  }
})