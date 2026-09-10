import { List, Paper, generateUtilityClass, styled, Theme, alpha, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


const MUI_NAME = 'EveliCustomerRatings';

export type RatingKey = 'count5' | 'count4' | 'count3' | 'count2' | 'count1';
type AverageLevel = 'good' | 'warning' | 'bad';

export interface EveliCustomerRatingsClasses {
  root: string;
  title: string;
  averageText: string;
  cellValue: string;
  nameColumn: string;
  columnWrapper: string;
  header: string;
  countsPanel: string;
  cell: string;
  row: string;
  totalSection: string;
  commentButton: string;
  dialogListItem: string;
  dialogListItemIcon: string;
}

export type EveliCustomerRatingsClassKey = keyof EveliCustomerRatingsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    averageText: ['averageText'],
    cellValue: ['cellValue'],
    nameColumn: ['nameColumn'],
    columnWrapper: ['columnWrapper'],
    header: ['header'],
    countsPanel: ['countsPanel'],
    cell: ['cell'],
    row: ['row'],
    totalSection: ['totalSection'],
    commentButton: ['commentButton'],
    dialogListItem: ['dialogListItem'],
    dialogListItemIcon: ['dialogListItemIcon'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliCustomerRatingsRoot = styled(Paper, {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => {
  return {
    padding: theme.spacing(2),
    marginTop: theme.spacing(2),

    [`& .${MUI_NAME}-title`]: {
      fontWeight: 600,
      marginBottom: theme.spacing(2),
    },

    [`& .${MUI_NAME}-cellValue`]: {
      ...theme.typography.subtitle2
    },

    [`& .${MUI_NAME}-averageText`]: {
      ...theme.typography.subtitle2,
      fontWeight: 'bold',
    },

    [`& .${MUI_NAME}-averageText[data-rps-avg="good"]`]: {
      color: theme.palette.success.main,
    },

    [`& .${MUI_NAME}-averageText[data-rps-avg="warning"]`]: {
      color: theme.palette.warning.main,
    },

    [`& .${MUI_NAME}-averageText[data-rps-avg="bad"]`]: {
      color: theme.palette.error.main,
    },

    [`& .${MUI_NAME}-nameColumn`]: {
      width: '20%',
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-totalSection`]: {
      marginLeft: theme.spacing(3),
      minWidth: '5%',
    },

    [`& .${MUI_NAME}-columnWrapper`]: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      marginLeft: theme.spacing(1),
      width: '7%',
    },

    [`& .${MUI_NAME}-header`]: {
      height: 50,
      display: 'flex',
      alignItems: 'center',
      paddingBottom: theme.spacing(1),
      '& .MuiTypography-root': {
        fontWeight: 500
      }
    },

    [`& .${MUI_NAME}-countsPanel`]: {
      width: '100%',

      [`& .${MUI_NAME}-cell`]: {
        justifyContent: 'center',
        paddingRight: 0,
        width: '100%',
      },
    },

    [`& .${MUI_NAME}-row:nth-of-type(2) .${MUI_NAME}-countsPanel`]: {
      borderTopLeftRadius: theme.spacing(1),
      borderTopRightRadius: theme.spacing(1),
    },

    [`& .${MUI_NAME}-row:last-of-type .${MUI_NAME}-countsPanel`]: {
      borderBottomLeftRadius: theme.spacing(1),
      borderBottomRightRadius: theme.spacing(1),
    },

    ...ratingBgColorRules(theme, `.${MUI_NAME}-countsPanel`),

    [`& .${MUI_NAME}-header[data-rps-key]`]: {
      justifyContent: 'center',
      paddingBottom: 0,
      '& .MuiSvgIcon-root': { fontSize: '2.5rem' },
    },

    ...ratingIconColorRules(theme, `.${MUI_NAME}-header`),

    [`& .${MUI_NAME}-cell`]: {
      height: 35,
      display: 'flex',
      alignItems: 'center',
      paddingRight: theme.spacing(1),
    },

    [`& .${MUI_NAME}-commentButton`]: {
      marginLeft: 'auto',
    },
  };
});



export const EveliCustomerRatingsDialogRoot = styled(List, {
  name: MUI_NAME,
  slot: 'DialogRoot',
})(({ theme }) => {
  return {
    [`& .${MUI_NAME}-dialogListItem`]: {
      paddingTop: theme.spacing(1.5),
      paddingBottom: theme.spacing(1.5),
      alignItems: 'flex-start',
    },

    [`& .${MUI_NAME}-dialogListItemIcon`]: {
      minWidth: 40,
      marginTop: theme.spacing(0.5),
    },

    ...ratingIconColorRules(theme, `.${MUI_NAME}-dialogListItemIcon`),
  };
});

export function getRpsColumnProps(key: RatingKey): { 'data-rps-key': RatingKey } {
  return { 'data-rps-key': key };
}

export function getAverageProps(average: number): { 'data-rps-avg': AverageLevel } {
  if (average < 3) {
    return { 'data-rps-avg': 'bad' };
  }
  if (average < 4) {
    return { 'data-rps-avg': 'warning' };
  }
  return { 'data-rps-avg': 'good' };
}

function ratingBgColorRules(theme: Theme, parentClass: string): Record<string, { backgroundColor: string }> {
  const bg: Record<RatingKey, string> = {
    count5: alpha(theme.palette.success.main, 0.15),
    count4: alpha(theme.palette.success.main, 0.08),
    count3: alpha(theme.palette.warning.main, 0.15),
    count2: alpha(theme.palette.error.main, 0.08),
    count1: alpha(theme.palette.error.main, 0.15),
  };
  const rules: Record<string, { backgroundColor: string }> = {};
  for (const key of Object.keys(bg) as RatingKey[]) {
    rules[`& ${parentClass}[data-rps-key="${key}"]`] = { backgroundColor: bg[key] };
  }
  return rules;
}

function ratingIconColorRules(theme: Theme, parentClass: string): Record<string, { color: string }> {
  const ic: Record<RatingKey, string> = {
    count5: theme.palette.success.main,
    count4: lighten(theme.palette.success.main, 0.3),
    count3: theme.palette.warning.main,
    count2: lighten(theme.palette.error.main, 0.2),
    count1: theme.palette.error.main,
  };
  const rules: Record<string, { color: string }> = {};
  for (const key of Object.keys(ic) as RatingKey[]) {
    rules[`& ${parentClass}[data-rps-key="${key}"] .MuiSvgIcon-root`] = { color: ic[key] };
  }
  return rules;
}

