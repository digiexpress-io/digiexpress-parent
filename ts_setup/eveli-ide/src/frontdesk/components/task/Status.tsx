import React from 'react';
import { defineMessages, FormattedMessage } from 'react-intl';
import { FieldInputProps } from 'formik';
import Indicator from './Indicator';
import RadioGroupPopover from './RadioGroupPopover';
import { ColorMap, COLORS } from './ColorMap';

const messages = defineMessages({
  NEW: {
    id: 'task.status.new',
    defaultMessage: 'New',
  },
  OPEN: {
    id: 'task.status.open',
    defaultMessage: 'Open',
  },
  COMPLETED: {
    id: 'task.status.completed',
    defaultMessage: 'Completed',
  },
  REJECTED: {
    id: 'task.status.rejected',
    defaultMessage: 'Rejected',
  },
});

type MessageKeyType = keyof typeof messages;

const colorMap: ColorMap = {
  NEW: COLORS.YELLOW,
  OPEN: COLORS.BLUE,
  COMPLETED: COLORS.GREEN,
  REJECTED: COLORS.GREY,
};

interface Props extends FieldInputProps<""> {
  label: string
  readonly?:boolean
  handleCallback?: (newValue: string) => void;
}

export const StatusComponent =({label, readonly, handleCallback,  ...props}:Props) =>{
 return (
  <RadioGroupPopover 
    label={label}
    readonly={readonly}
    messages={messages}
    colorMap={colorMap}
    handleCallback={handleCallback}
    {...props}
  />
 );
}

type ViewProps = {
  value: MessageKeyType | undefined
  [x:string]: any
}
export const StatusViewComponent:React.FC<ViewProps> = ({value, ...restProps})=> {
  if (!value) {
    return null;
  }
  const color = colorMap[value];
  return (
    <Indicator color={color ? color : undefined} {...restProps}>
      <FormattedMessage {...messages[value]}/>
    </Indicator>
  );
}