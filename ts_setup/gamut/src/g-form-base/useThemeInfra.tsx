import { generateUtilityClass, styled, useThemeProps } from '@mui/material'
import composeClasses from "@mui/utils/composeClasses";

import { useVariantOverride } from '../api-variants'
import { useForm } from "../api-dialob";
import { GFormBaseProps } from './GFormBase';


const MUI_NAME = 'GFormBase';

export function useThemeInfra(initProps: GFormBaseProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { store, onAfterComplete } = useForm();
  const { form } = store;
  const element = form.getItem(props.id)!;

  const unwrap = element.type === 'survey' && element.view === 'survey';
  const ownerState = {
    ...props,
    variant: props.id,
    unwrap,

  }
  const classes = useUtilityClasses(props.id, element.type);

  return { classes, ownerState, props, form, onAfterComplete,
    formStore: store,
    actionItem: element
   };
}



// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormBaseRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles),
    ];
  },
})<{ ownerState: { variant: string }}>(({ theme }) => {
  return {

  };
});


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (itemId: string, type: string) => {
  const variant = itemId;
  const slots = {
    root: [
      'root',
      type,
      variant
    ],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}