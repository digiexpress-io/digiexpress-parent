import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { BatchViewHeadersProps } from "./BatchViewHeaders";

export const MUI_NAME = 'BatchViewHeaders';

export type BatchViewHeadersClassKey = keyof BatchViewHeadersClasses;

export interface BatchViewHeadersClasses {
  root: string;
  stepSection: string;
}

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    stepSection: ['stepSection'],
    instanceSection: ['instanceSection'],
    title: ['title']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const BatchViewHeadersRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Headers',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.stepSection,
      styles.instanceSection,
      styles.title
    ];
  },
})<{ ownerState: BatchViewHeadersProps }>(({ theme, ownerState }) => {
  return {
    display: 'flex',
    gap: theme.spacing(1),
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingRight: theme.spacing(1),
    backgroundColor: theme.palette.secondary.dark,
    width: 'fit-content',

    '& .BatchViewHeaders-stepSection': {
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      alignItems: 'center',
      backgroundColor: theme.palette.background.paper,
      border: `1px solid ${theme.palette.divider}`,
      borderRadius: theme.spacing(0.5),
      padding: theme.spacing(2),
      height: '100px',
      width: ownerState.stepSectionWidth
    },
    '& .BatchViewHeaders-instanceSection': {
      display: 'flex',
      justifyContent: 'right',
      alignItems: 'flex-end',
      padding: theme.spacing(2),
      width: ownerState.instanceSectionWidth
    },
    '& .BatchViewHeaders-title': {
      fontWeight: 'bold'
    }
  }
})