import { styled, generateUtilityClass, alpha, lighten } from '@mui/material';
import { Box, Paper, TableCell, Typography } from '@mui/material';
import { Theme } from '@mui/material/styles';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'EveliCustomerRatings';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    headerText: ['headerText'],
    bodyText: ['bodyText'],
    iconCell: ['iconCell'],
    colorCell: ['colorCell'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliCustomerRatingsHeaderText = styled(Typography, {
  name: MUI_NAME,
  slot: 'HeaderText',
  overridesResolver: (_props, styles) => [styles.headerText],
})(({ theme }) => ({
  ...theme.typography.body2,
  fontWeight: 'bold',
}));

export const EveliCustomerRatingsBodyText = styled(Typography, {
  name: MUI_NAME,
  slot: 'BodyText',
  overridesResolver: (_props, styles) => [styles.bodyText],
})(({ theme }) => ({
  ...theme.typography.body2,
}));

export const EveliCustomerRatingsRoot = styled(Paper, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root],
})(({ theme }) => ({
  padding: theme.spacing(2),
  marginTop: theme.spacing(2),
}));

export const EveliCustomerRatingsTitle = styled(Typography, {
  name: MUI_NAME,
  slot: 'Title',
  overridesResolver: (_props, styles) => [styles.title],
})(({ theme }) => ({
  fontWeight: 600,
  marginBottom: theme.spacing(2),
}));

export const EveliCustomerRatingsIconCell = styled(Box, {
  name: MUI_NAME,
  slot: 'IconCell',
  overridesResolver: (_props, styles) => [styles.iconCell],
  shouldForwardProp: (prop) => prop !== 'iconColor',
})<{ iconColor: string }>(({ iconColor }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  '& .MuiSvgIcon-root': {
    color: iconColor,
    fontSize: '1.5rem',
  },
}));

export const EveliCustomerRatingsColorCell = styled(TableCell, {
  name: MUI_NAME,
  slot: 'ColorCell',
  overridesResolver: (_props, styles) => [styles.colorCell],
  shouldForwardProp: (prop) => prop !== 'bgColor',
})<{ bgColor: string }>(({ bgColor }) => ({
  backgroundColor: bgColor,
}));

export function averageBg(theme: Theme, average: number): string {
  if (average <= 2.5) { return alpha(theme.palette.error.main, 0.1); }
  if (average <= 3.5) { return alpha(theme.palette.warning.main, 0.1); }
  return alpha(theme.palette.success.main, 0.1);
}

export function getRpsColumnBg(theme: Theme): { count5: string; count4: string; count3: string; count2: string; count1: string } {
  return {
    count5: alpha(theme.palette.success.main, 0.3),
    count4: alpha(theme.palette.success.main, 0.1),
    count3: alpha(theme.palette.warning.main, 0.3),
    count2: alpha(theme.palette.error.main, 0.1),
    count1: alpha(theme.palette.error.main, 0.3),
  };
}

export function getRpsColumnIconColor(theme: Theme): { count5: string; count4: string; count3: string; count2: string; count1: string } {
  return {
    count5: theme.palette.success.main,
    count4: lighten(theme.palette.success.main, 0.4),
    count3: theme.palette.warning.main,
    count2: lighten(theme.palette.error.main, 0.3),
    count1: theme.palette.error.main,
  };
}
