import { generateUtilityClass, Stack, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/eveli-api';


export const MUI_NAME = 'EveliMetisStatus';

export interface EveliMetisStatusClasses {
  root: string;
  heading: string;
  card: string;
  row: string;
  actions: string;
  progress: string;
}

export type EveliMetisStatusClassKey = keyof EveliMetisStatusClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    heading: ['heading'],
    card: ['card'],
    row: ['row'],
    actions: ['actions'],
    progress: ['progress']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const EveliMetisStatusRoot = styled(Stack, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.heading,
      styles.card,
      styles.row,
      styles.actions,
      styles.progress,
      ...useVariantOverride(props, styles)
    ];
  },
})(({ theme }) => {
  return {
    gap: theme.spacing(2),
    maxWidth: '720px',

    '& .EveliMetisStatus-heading': {
      marginBottom: theme.spacing(1)
    },

    '& .EveliMetisStatus-card': {
      padding: theme.spacing(2)
    },

    '& .EveliMetisStatus-row': {
      display: 'grid',
      gridTemplateColumns: '180px 1fr',
      columnGap: theme.spacing(2),
      rowGap: theme.spacing(0.5),
      alignItems: 'baseline',
      marginBottom: theme.spacing(1)
    },

    '& .EveliMetisStatus-row .MuiChip-root': {
      width: 'fit-content'
    },

    '& .EveliMetisStatus-actions': {
      display: 'flex',
      gap: theme.spacing(1),
      flexWrap: 'wrap',
      marginTop: theme.spacing(2)
    },

    '& .EveliMetisStatus-progress': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    }
  };
});
