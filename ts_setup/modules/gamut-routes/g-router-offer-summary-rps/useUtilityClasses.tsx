import { generateUtilityClass, styled, TextField } from "@mui/material";
import { alpha, lighten } from "@mui/material/styles";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GRouterOfferSummaryRps';

export interface GRouterOfferSummaryRpsClasses {
  root: string;
  title: string;
  faces: string;
  faceItem: string;
  faceItemTerrible: string;
  faceItemPoor: string;
  faceItemOkay: string;
  faceItemGood: string;
  faceItemExcellent: string;
  faceLabel: string;
}
export type GRouterOfferSummaryRpsClassKey = keyof GRouterOfferSummaryRpsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    faces: ['faces'],
    faceItem: ['faceItem'],
    faceItemTerrible: ['faceItemTerrible'],
    faceItemPoor: ['faceItemPoor'],
    faceItemOkay: ['faceItemOkay'],
    faceItemGood: ['faceItemGood'],
    faceItemExcellent: ['faceItemExcellent'],
    faceLabel: ['faceLabel'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const GRouterOfferSummaryRpsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root],
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: theme.spacing(2),
  marginTop: theme.spacing(2),
  marginBottom: theme.spacing(2),
  padding: theme.spacing(1),
  boxShadow: `0 0 20px 4px ${alpha(theme.palette.text.secondary, 0.1)}`,

  [`.${MUI_NAME}-title`]: {
    ...theme.typography.h3,
  },
  [`.${MUI_NAME}-faces`]: {
    display: 'flex',
    [theme.breakpoints.up('md')]: {
      flexDirection: 'row',
      flexWrap: 'wrap',
      justifyContent: 'center',
      gap: theme.spacing(4),
    },
    [theme.breakpoints.down('md')]: {
      flexDirection: 'column',
      gap: theme.spacing(0.5),
      width: '100%',
    },
    '& .MuiSvgIcon-root': {
      [theme.breakpoints.down('md')]: { fontSize: '2.5rem' },
      [theme.breakpoints.up('md')]: { fontSize: '3rem' },
    },
  },
  [`.${MUI_NAME}-faceItem`]: {
    display: 'flex',
    alignItems: 'center',
    transition: 'background-color 150ms cubic-bezier(0.4, 0, 0.2, 1)',
    [theme.breakpoints.up('md')]: {
      flexDirection: 'column',
      justifyContent: 'center',
      borderRadius: theme.spacing(1),
      padding: theme.spacing(1),
    },
    [theme.breakpoints.down('md')]: {
      flexDirection: 'row',
      justifyContent: 'flex-start',
      width: '100%',
      borderRadius: theme.spacing(0.5),
      padding: theme.spacing(0.5, 1),
    },
  },
  [`.${MUI_NAME}-faceLabel`]: {
    [theme.breakpoints.up('md')]: {
      ...theme.typography.caption,
      textAlign: 'center',
    },
    [theme.breakpoints.down('md')]: {
      ...theme.typography.body2,
      marginLeft: theme.spacing(1.5),
    },
  },
  [`.${MUI_NAME}-faceItemTerrible`]: {
    '& .MuiSvgIcon-root': {
      color: theme.palette.error.main,
    },
    '&:hover, &[data-selected="true"]': {
      backgroundColor: alpha(theme.palette.error.main, 0.1),
    },
  },
  [`.${MUI_NAME}-faceItemPoor`]: {
    '& .MuiSvgIcon-root': {
      color: lighten(theme.palette.error.main, 0.3),
    },
    '&:hover, &[data-selected="true"]': {
      backgroundColor: alpha(theme.palette.error.main, 0.1),
    },
  },
  [`.${MUI_NAME}-faceItemOkay`]: {
    '& .MuiSvgIcon-root': {
      color: theme.palette.warning.main,
    },
    '&:hover, &[data-selected="true"]': {
      backgroundColor: alpha(theme.palette.warning.main, 0.1),
    },
  },
  [`.${MUI_NAME}-faceItemGood`]: {
    '& .MuiSvgIcon-root': {
      color: lighten(theme.palette.success.main, 0.4),
    },
    '&:hover, &[data-selected="true"]': {
      backgroundColor: alpha(theme.palette.success.main, 0.1),
    },
  },
  [`.${MUI_NAME}-faceItemExcellent`]: {
    '& .MuiSvgIcon-root': {
      color: theme.palette.success.main,
    },
    '&:hover, &[data-selected="true"]': {
      backgroundColor: alpha(theme.palette.success.main, 0.1),
    },
  },
}));


export const GRouterOfferSummaryRpsTextField = styled(TextField, {
  name: MUI_NAME,
  slot: 'TextField',
})(({ theme }) => ({
  '& .MuiOutlinedInput-root': { borderRadius: 0 },
  '& .MuiInputBase-input': { height: '2.5rem', padding: '0 12px' },
  '& .MuiInputBase-multiline': { paddingLeft: 0, paddingRight: 0, paddingTop: theme.spacing(1), paddingBottom: theme.spacing(1) },
}));
