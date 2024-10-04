
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';
import { GInputSurveyQuestionProps } from './GInputSurveyQuestion';


export const MUI_NAME = 'GInputSurveyQuestion';


export const useUtilityClasses = (itemId: string) => {
  const slots = {
    root: ['root', itemId],
    body: ['body'],
    label: ['label'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputSurveyQuestionBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',

  shouldForwardProp: (prop) => ( 
    prop !== 'id' && 
    prop !== 'label' && 
    prop !== 'options' && 
    prop !== 'description' && 
    prop !== 'unwrap'),

  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<Omit<GInputSurveyQuestionProps, 'onChange'>>(({ theme, index }) => {
  const even = index % 2 === 0;
  return {
    backgroundColor: even ? undefined : theme.palette.background.paper,
    textAlign: 'center'

  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputSurveyQuestionLabel = styled('div', {
  name: MUI_NAME,
  slot: 'Label',

  shouldForwardProp: (prop) => ( 
    prop !== 'id' && 
    prop !== 'label' && 
    prop !== 'options' && 
    prop !== 'description' && 
    prop !== 'unwrap'),

  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<Omit<GInputSurveyQuestionProps, 'onChange'>>(({ theme, index }) => {
  return {
    padding: theme.spacing(1),
    fontWeight: '600',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end'
  };
});