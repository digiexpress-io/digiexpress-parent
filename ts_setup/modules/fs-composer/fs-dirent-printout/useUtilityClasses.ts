import { generateUtilityClass, styled } from '@mui/material';
  import composeClasses from '@mui/utils/composeClasses';
  import { FsColors } from '../fs-theme';

  const MUI_NAME = 'FsDirentPrintout';

  export interface FsDirentPrintoutClasses {
    root: string;
    titleRow: string;
    title: string;
    formContainer: string;
    pageList: string;
  }

  export type FsDirentPrintoutClassKey = keyof FsDirentPrintoutClasses;

  export const useUtilityClasses = () => {
    const slots = {
      root: ['root'],
      titleRow: ['titleRow'],
      title: ['title'],
      formContainer: ['formContainer'],
      pageList: ['pageList'],
    };
    const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
    return composeClasses(slots, getUtilityClass, {});
  };

  export const FsDirentPrintoutRoot = styled('div', {
    name: MUI_NAME,
    slot: 'Root',
    shouldForwardProp: (prop) => prop !== 'ownerState',
  })(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',

    [`& .${MUI_NAME}-titleRow`]: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: theme.spacing(2),
    },

    [`& .${MUI_NAME}-title`]: {
      ...theme.typography.body1,
      color: FsColors.light.text,
      marginBottom: theme.spacing(2),
      fontWeight: 500
    },

    [`& .${MUI_NAME}-formContainer`]: {
      display: 'flex',
      flexDirection: 'column',
    },

    [`& .${MUI_NAME}-pageList`]: {
      display: 'flex',
      flexDirection: 'column',
      gap: theme.spacing(0.5),
    },

  }));