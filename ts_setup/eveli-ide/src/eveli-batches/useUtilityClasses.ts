import { BatchApi } from "@/api-batch";
import { alpha, generateUtilityClass, Paper, Stack, styled, Theme } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";

export const MUI_NAME = 'EveliBatchView';

export type EveliBatchViewClassKey = keyof EveliBatchViewClasses;

export interface EveliBatchViewClasses {
  root: string;
  stepSlot: string;
  instanceSlot: string;
  instanceContainer: string;
  instanceDateTime: string;
  batchNameRow: string
}

export interface SectionWidth { 
  instanceSectionWidth: string,
  stepSectionWidth: string
}
// used in both headers and "body" to ensure that headers and instance/step boxes maintain same size
export const sectionWidth: SectionWidth = {
  instanceSectionWidth: '300px',
  stepSectionWidth: '200px'
}

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    stepSlot: ['stepSlot'],
    instanceSlot: ['instanceSlot'],
    instanceContainer: ['instanceContainer'],
    instanceDateTime: ['instanceDateTime'],
    batchNameRow: ['batchNameRow']
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
      styles.batchNameRow
    ];
  },
})<{}>(({ theme, }) => {
  return {
    gap: theme.spacing(1),

    '& .EveliBatchView-batchNameRow': {
      display: 'flex',
      width: '50%',
      justifyContent: 'space-between'
    }

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
      borderRadius: theme.spacing(0.5),
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
});


export const StyledStepSlot = styled(Paper, {
  name: MUI_NAME,
  slot: 'Step',
  overridesResolver: (props, styles) => {
    return [
      styles.stepSlot,
    ];
  },
})<{ value: BatchApi.RuntimeStep }>(({ theme, value }) => {

  const bg_color = getStepBackgroundColor(value.status, theme)

  return {
    padding: theme.spacing(2),
    width: sectionWidth.stepSectionWidth,
    backgroundColor: bg_color,
    borderRadius: theme.spacing(0.5),

  }
});

function getStepBackgroundColor(status: BatchApi.RuntimeStatus, theme: Theme): string {
  switch (status) {
    case 'CANCELLED': {
      return `${alpha(theme.palette.action.disabled, 0.05)}`
    }
    case 'COMPLETED': {
      return `${alpha(theme.palette.success.main, 0.1)}`
    }
    case 'CREATED': {
      return theme.palette.background.paper
    }
    case "EXECUTING": {
      return theme.palette.background.paper
    }
    case 'SKIPPED': {
      return `${alpha(theme.palette.action.disabled, 0.05)}`
    }
    default: {
      return theme.palette.background.paper;
    }
  }

}