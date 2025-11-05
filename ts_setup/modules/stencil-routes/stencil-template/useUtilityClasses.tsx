import { styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const TemplateComposerClassName = 'TemplateComposer';

export interface TemplateComposerClasses {
  root: string;
  nameDesc: string;
  sectionTitle: string;
  editorRow: string;
  editorCol: string;
  helperText: string;
}
export type TemplateComposerClassKey = keyof TemplateComposerClasses;

export const useTemplateComposerUtilityClasses = () => {
  const slots = {
    root: ['root'],
    nameDesc: ['nameDesc'],
    sectionTitle: ['sectionTitle'],
    editorRow: ['editorRow'],
    editorCol: ['editorCol'],
    helperText: ['helperText']
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
    styles.nameDesc,
    styles.sectionTitle,
    styles.editorRow,
    styles.editorCol,
  ],
})(({ theme }) => ({
  '& .TemplateComposer-nameDesc': {
    margin: 0,
  },
  '& .TemplateComposer-helperText': {
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
