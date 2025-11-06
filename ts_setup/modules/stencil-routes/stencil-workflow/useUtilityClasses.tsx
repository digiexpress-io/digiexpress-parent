import { styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

/** ---- WorkflowDelete ---- */
export const WorkflowDeleteClassName = 'WorkflowDelete';

export interface WorkflowDeleteClasses {
  root: string;
  description: string;
  infoBox: string;
  label: string;
  value: string;
}
export type WorkflowDeleteClassKey = keyof WorkflowDeleteClasses;

export const useWorkflowDeleteUtilityClasses = () => {
  const slots = {
    root: ['root'],
    description: ['description'],
    infoBox: ['infoBox'],
    label: ['label'],
    value: ['value'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(WorkflowDeleteClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const WorkflowDeleteRoot = styled('div', {
  name: WorkflowDeleteClassName,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root,
    styles.description,
    styles.infoBox,
    styles.label,
    styles.value,
  ],
})(({ theme }) => ({
  '& .WorkflowDelete-description': {
    marginBottom: theme.spacing(1),
  },
  '& .WorkflowDelete-infoBox': {
    padding: theme.spacing(1),
    borderRadius: theme.shape.borderRadius,
    backgroundColor: theme.palette.action.hover,
    wordBreak: 'break-word',
    fontFamily: 'monospace',
  },
  '& .WorkflowDelete-label': {
    fontWeight: 700,
  },
  '& .WorkflowDelete-value': {},
}));
