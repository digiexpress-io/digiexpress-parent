import { styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const TemplateComposerClassName = 'TemplateComposer';

export interface TemplateComposerClasses {
  root: string;
  nameHelperRow: string;
  nameDesc: string;
  nameError: string;
  sectionTitle: string;
  editorRow: string;
  editorCol: string;
}
export type TemplateComposerClassKey = keyof TemplateComposerClasses;

export const useTemplateComposerUtilityClasses = () => {
  const slots = {
    root: ['root'],
    nameHelperRow: ['nameHelperRow'],
    nameDesc: ['nameDesc'],
    nameError: ['nameError'],
    sectionTitle: ['sectionTitle'],
    editorRow: ['editorRow'],
    editorCol: ['editorCol'],
  };
  const getUtilityClass = (slot: string) =>
    generateUtilityClass(TemplateComposerClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const useUtilityClasses = useTemplateComposerUtilityClasses;

export const TemplateComposerRoot = styled('div', {
  name: TemplateComposerClassName,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root,
    styles.nameHelperRow,
    styles.nameDesc,
    styles.nameError,
    styles.sectionTitle,
    styles.editorRow,
    styles.editorCol,
  ],
})(({ theme }) => ({
  '& .TemplateComposer-nameHelperRow': {
    display: 'flex',
    justifyContent: 'flex-start',
    marginTop: theme.spacing(0.5),
    marginLeft: theme.spacing(2),
    gap: theme.spacing(1),
  },

  '& .TemplateComposer-nameDesc': {
    margin: 0,
  },

  '& .TemplateComposer-nameError': {
    margin: 0,
    marginLeft: theme.spacing(2),
  },

  '& .TemplateComposer-sectionTitle': {
    marginTop: theme.spacing(2),
    fontWeight: 700,
  },

  '& .TemplateComposer-editorRow': {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: theme.spacing(2),
  },

  '& .TemplateComposer-editorCol': {
    flex: 1,
    paddingRight: theme.spacing(1),
  },
}));
