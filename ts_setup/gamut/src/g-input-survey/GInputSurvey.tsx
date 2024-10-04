import React from 'react'
import { useThemeProps, Box } from '@mui/material'
import { MUI_NAME, useUtilityClasses, GInputSurveyRoot, GInputSurveyBody } from './useUtilityClasses'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputBase, GInputBaseProps, LabelPosition } from '../g-input-base'
import { GInputAdornment } from '../g-input-adornment'
import { GInputSurveyOption } from './GInputSurveyOption'
import { DialobApi } from '../api-dialob'



export interface GInputSurveyProps {
  id: string;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;
  children: React.ReactNode;

  options: { id: string, label: string, description?: undefined | string }[];
  questions: { id: string, label: string, description?: undefined | string, value: undefined | string }[];
  errors?: DialobApi.ActionError[] | undefined;
  
  vertical?: boolean | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;

  component?: React.ElementType<GInputSurveyProps>;
  slots?: {
    option: React.ElementType<GInputSurveyOptionProps>;
    body: React.ElementType<GInputSurveyProps>;
  };
}

/** SLOT PROPS */
export interface GInputSurveyOptionProps {
  id: string;
  index: number;
  label: string | undefined;
  description: string | undefined;
}


export const GInputSurvey: React.FC<GInputSurveyProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, vertical = false, labelPosition, errors } = props;
  const ownerState = { ...props, vertical };
  const classes = useUtilityClasses(id);


  const slots: GInputBaseProps<GInputSurveyProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: () => <></>,
      adornment: GInputAdornment,
      secondary: Options
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      secondary: { ...ownerState, name: id },
      adornment: { id, children: props.description, title: label }
    }
  }

  return (<GInputSurveyRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputSurveyRoot>);
}

const Options: React.FC<GInputSurveyProps> = (props) => {
  const { id, options } = props;
  const classes = useUtilityClasses(id);
  
  const Body: React.ElementType<GInputSurveyProps> = props.slots?.body ?? GInputSurveyBody as any;
  const Option: React.ElementType<GInputSurveyOptionProps> = props.slots?.option ?? GInputSurveyOption as any;

  return (
    
    <Body {...props} className={classes.body}>
      <div />
      {options.map((e, index) => (<Option index={index} id={id} key={e.label} label={e.label} description={e.description} className={classes.option} />))}
      {props.children}
    </Body>);
}
