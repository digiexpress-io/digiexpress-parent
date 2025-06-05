import { styled, generateUtilityClass, Box, lighten, alpha } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'


export const EveliUserProfileClassName = 'EveliUserProfileBase';


export const MUI_NAME = 'EveliUserProfile';
export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    sectionTitle: ['sectionTitle'],
    divider: ['divider']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliUserProfileRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.sectionTitle,
      styles.divider
    ];
  },
})(({ theme }) => {

  return {
    display: 'flex',
    '& .EveliUserProfile-divider': {
      marginBottom: theme.spacing(3)
    },
    '& .EveliUserProfile-sectionTitle': {
      display: 'flex',
      marginBottom: theme.spacing(1),
      '& .MuiTypography-root': {
        flexDirection: 'row',
        fontWeight: 'bold',
      },
      '& .MuiSvgIcon-root': {
        color: theme.palette.primary.main,
        marginRight: theme.spacing(1)
      }
    },


  };
});


export const EveliUserOverviewDetail = styled(Box, {
  name: MUI_NAME,
  slot: 'OverviewItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.overviewItem
    ];
  },
})<{}>(({ theme }) => {

  return {
    padding: theme.spacing(2),
    height: '100%',
  
  };
});


