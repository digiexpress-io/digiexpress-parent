import React from 'react';
import { defineMessages, FormattedMessage } from 'react-intl';
import { FieldInputProps } from 'formik';
import { ColorMap, COLORS } from './ColorMap';
import RadioGroupPopover from './RadioGroupPopover';
import Indicator from './Indicator';

const messages = defineMessages({
  LOW: {
    id: 'task.priority.low',
    defaultMessage: 'Low',
  },
  NORMAL: {
    id: 'task.priority.normal',
    defaultMessage: 'Normal',
  },
  HIGH: {
    id: 'task.priority.high',
    defaultMessage: 'High',
  },
});

type PriorityType = keyof typeof messages;

const colorMap:ColorMap = {
  LOW: COLORS.GREEN,
  NORMAL: COLORS.BLUE,
  HIGH: COLORS.RED,
};

interface Props extends FieldInputProps<""> {
  label: string
  readonly?:boolean
}

export const Priority = ({ label, readonly, ...props }: Props) => {
  
  return (
    <RadioGroupPopover 
      label={label}
      readonly={readonly}
      messages={messages}
      colorMap={colorMap}
      {...props}
    />
  );
}

type ViewProps = {
  value: PriorityType | undefined
  [x:string]: any
}

export const PriorityView:React.FC<ViewProps> = ({value, ...restProps})=>{
  if (!value) {
    return null;
  }
  const messageKey: PriorityType|undefined = value;
  return (
    <Indicator color={colorMap[value]} {...restProps}>
      <FormattedMessage {...messages[messageKey]}/>
    </Indicator>
  );
}
