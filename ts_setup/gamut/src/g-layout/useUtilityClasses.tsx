import React from 'react';
import composeClasses from '@mui/utils/composeClasses';
import { styled, generateUtilityClass } from '@mui/material';
import { GLayoutProps } from './GLayout';


export const MUI_NAME = 'GLayout';

export const useUtilityClasses = (ownerState: GLayoutProps) => {
  const slots = {
    root: ['root', ownerState.variant],
    left: ['left'],
    right: ['right'],
    topTitle: ['topTitle'],
    breadcrumbs: ['breadcrumbs'],
    toolbar: ['toolbar'],
    buttonRow: ['buttonRow'],

    oneColContent: ['oneColContent'],
    oneColContentSmall: ['oneColContentSmall'],

    fillSessionStartEndLayout: ['fillSessionStartEndLayout'],
    fillSessionStartEnd: ['fillSessionStartEnd'],
    fillSessionStartEndTopTitle: ['fillSessionStartEndTopTitle'],
    fillSessionStartEndChildren: ['fillSessionStartEndChildren']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLayoutRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.row,
      styles.topTitle,
      styles.breadcrumbs,
      styles.left,
      styles.right,
      styles.toolbar,
      styles.buttonRow,

      styles.oneColContent,
      styles.oneColContentSmall,

      styles.fillSessionStartEndLayout,
      styles.fillSessionStartEnd,
      styles.fillSessionStartEndTopTitle,
      styles.fillSessionStartEndChildren
    ];
  },
})(({ theme }) => {

  return {
    width: "100%",
    '& .GLayout-topTitle': {
      paddingLeft: theme.spacing(2),
      margin: theme.spacing(1),
    },
    '& .GLayout-breadcrumbs': {
      paddingLeft: theme.spacing(1),
    },
    '& .GLayout-left': {
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(1),
      paddingBottom: theme.spacing(1)
    },
    '& .GLayout-right': {
    },
    '& .GLayout-oneColContent': {
      padding: 'unset'
    },
    '& .GLayout-fillSessionStartEndLayout': {
      justifyContent: 'center'
    },
    '& .GLayout-fillSessionStartEnd': {
      [theme.breakpoints.up('md')]: {
        padding: theme.spacing(5),
        marginTop: theme.spacing(4),
      },
      [theme.breakpoints.down('md')]: {
        padding: theme.spacing(2),
        margin: theme.spacing(1),
      },
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      border: `1px solid ${theme.palette.divider}`,
      backgroundColor: theme.palette.background.default
    },
    '& .GLayout-fillSessionStartEndTopTitle': {
      marginBottom: theme.spacing(5),
    },
    '& .GLayout-fillSessionStartEndChildren': {
      [theme.breakpoints.up('sm')]: {
        display: 'flex',
        gap: theme.spacing(1),
      },
      [theme.breakpoints.down('sm')]: {
        display: 'flex',
        flexDirection: 'column',
        width: '100%',
        gap: theme.spacing(1),
      },
    },

    '& .GLayout-oneColContentSmall': {
      padding: theme.spacing(1)
    },
    '& .GLayout-toolbar': {
      display: 'flex',
      [theme.breakpoints.down('md')]: {
        flexDirection: 'column'
      },
      paddingLeft: theme.spacing(2),
      margin: theme.spacing(1),
    },
    '& .GLayout-buttonRow': {
      [theme.breakpoints.down('md')]: {
        display: 'flex',
        width: '100%',
      },

    },
  };
});


/**
 * One row in the bar
 */
export const GLayoutRow = styled('div', {
  name: MUI_NAME,
  slot: 'Row',
  overridesResolver: (_props, styles) => {
    return [
      styles.row,
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    alignItems: 'center',
    width: '100%',
    paddingTop: theme.spacing(0.5),
    paddingLeft: theme.spacing(3),
    paddingRight: theme.spacing(3),
  };
});

/**
 * Calculate row and column based layout
 */
export function useGLayoutRows(init: React.ReactNode) {
  const children = React.Children.toArray(init);

  const rows: { left: React.ReactNode, right: React.ReactNode }[] = children.reduce((acc, item, index) => {
    const rowEnded = (index + 1) % 2 === 0; // index starts from 0
    if (rowEnded) {
      acc[index - 1].right = item;
    } else {
      acc[index] = { left: item, right: <></> }
    }
    return acc;
  }, [] as { left: React.ReactNode, right: React.ReactNode }[]);

  return rows;
}