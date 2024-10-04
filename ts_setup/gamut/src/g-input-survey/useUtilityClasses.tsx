
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';
import { GInputSurveyOptionProps, GInputSurveyProps } from './GInputSurvey';

export const MUI_NAME = 'GInputSurvey';


export const useUtilityClasses = (itemId: string) => {
  const slots = {
    root: ['root', itemId],
    body: ['body'],
    option: ['option'],
    question: ['question'],
    label: ['label'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInputSurveyRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GInputSurveyProps }>(({ theme, ownerState }) => {
  const { options, questions, vertical } = ownerState;
  const optionsCount = options.length;
  const rowCount = vertical ? optionsCount + 1 : questions.length;
  const colCount = vertical ? questions.length : optionsCount;

  return {
    '& .GInputSurvey-body': {
      display: 'grid', 
      alignItems: 'center', 
      
      gridAutoFlow: vertical ? 'column': 'row', 
      gridTemplateRows: `repeat(${rowCount}, auto)`, 
      gridTemplateColumns: `50% repeat(${colCount}, fit-content(30%))`,
    },
    '& .GInputSurvey-option': {
      padding: vertical ? undefined : theme.spacing(1),
      justifyContent: vertical ? 'flex-end' : undefined,
      display: vertical ? 'flex' : undefined,
      paddingRight: vertical ? theme.spacing(2) : undefined,
    }
  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputSurveyBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',

  shouldForwardProp: (prop) => (
    prop !== 'onChange' && 
    prop !== 'id' && 
    prop !== 'label' && 
    prop !== 'options' && 
    prop !== 'description' && 
    prop !== 'vertical' && 
    prop !== 'labelPosition' && 
    prop !== 'questions'),
    
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<GInputSurveyProps>(({ theme, options }) => {
  return {

  };
});
