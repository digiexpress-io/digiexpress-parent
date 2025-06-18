import { BatchApi } from "@/api-batch";
import { alpha, generateUtilityClass, Stack, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";

export const MUI_NAME = 'EveliBatchView';

export type EveliBatchViewClassKey = keyof EveliBatchViewClasses;

export interface EveliBatchViewClasses {
  root: string;
  stepSlot: string;
  instanceSlot: string;
  instanceContainer: string;
  instanceDateTime: string;
}

interface SectionWidth {
  instanceSectionWidth: string,
  stepSectionWidth: string
}
const sectionWidth: SectionWidth = {
  instanceSectionWidth: '300px',
  stepSectionWidth: '200px'
}

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    stepSlot: ['stepSlot'],
    instanceSlot: ['instanceSlot'],
    instanceContainer: ['instanceContainer'],
    instanceDateTime: ['instanceDateTime']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const EveliBatchViewRoot = styled(Stack, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,

    ];
  },
})<{}>(({ theme, }) => {
  return {
    gap: theme.spacing(1),


  }
})

export const StyledInstanceSlot = styled(Stack, {
  name: MUI_NAME,
  slot: 'Instance',
  overridesResolver: (props, styles) => {
    return [
      styles.instanceSlot,
      styles.instanceContainer,
      styles.instanceDateTime
    ];
  },
})<{ ownerState: BatchApi.RuntimeInstance }>(({ theme, ownerState }) => {

  const instance = ownerState;
  const isOk = instance.executionStatus === 'OK';
  const bg_color = isOk ? alpha(theme.palette.success.main, 0.1) : alpha(theme.palette.error.light, 0.1);

  return {
    gap: theme.spacing(1),
    display: 'flex',
    flexDirection: 'row',

    '& .EveliBatchView-instanceContainer': {
      padding: theme.spacing(2),
      backgroundColor: bg_color,
      width: sectionWidth.instanceSectionWidth
    },
    '& .EveliBatchView-instanceContainer .MuiChip-root': {
      marginBottom: theme.spacing(1),
      backgroundColor: isOk ? theme.palette.primary.main : theme.palette.error.dark,
      color: theme.palette.getContrastText(isOk ? theme.palette.primary.main : theme.palette.error.main)
    },
    '& .EveliBatchView-instanceDateTime': {
      width: '100px',
      height: '60px',
      borderRadius: '5px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }
  }
})