import React from 'react'
import { Typography, Button } from '@mui/material';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import { GInputBaseAnyProps } from '../g-input-base';

import { useUtilityClasses, GInput } from './useUtilityClasses';
import { GInputMultilistProps } from './g-input-multilist-types';



const Checkbox: React.FC<{
  optionKey: string,
  optionValue: string,
  ownerState: GInputBaseAnyProps & GInputMultilistProps,
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void
}> = (props) => {
  const ref = React.useRef<HTMLInputElement>(null); 

  const {ownerState, optionKey, optionValue, onChange } = props;
  const { value, keys, variant, id, disabled } = ownerState;
  const checked = value?.includes(optionKey) ?? false;
  const classes = useUtilityClasses(id, variant);


  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);


  function toggleInput() {
    const event = new Event('input', { bubbles: true });
    ref.current?.dispatchEvent(event);
  }

  function doNothing() {
  }

  return (
    <Button className={classes.option} variant='outlined'
      disabled={disabled}
      onClick={toggleInput}
      startIcon={checked ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />}>

      <Typography className={classes.optionTitle}>{keys && optionKey} {optionValue}</Typography>

      <input hidden value={optionKey} ref={ref} onChange={doNothing} />
    </Button>)

}

export const CheckboxList: React.FC<GInputBaseAnyProps & GInputMultilistProps> = (props) => {
  const { datasource } = props;
  const classes = useUtilityClasses(props.id, props.variant);
  const [value, setValue] = React.useState(props.value ?? null);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {      
    const multichoiceEvent = event as React.ChangeEvent<HTMLInputElement>;
    const targetValue = multichoiceEvent.target.value as string;

    const oldValue: string[] = value ?? [];
    const newValue: string[] = oldValue.includes(targetValue) ? 
      oldValue.filter(v => v !== targetValue) : 
      [...oldValue, targetValue];
    setValue(newValue);
  }

  return (
    <GInput className={classes.input}>
      <InputHidden id={props.id} value={value} onChange={props.onChange}/>
      <div className={classes.list}>
      {datasource.entries.map(({ key, value }) => (
        <Checkbox key={key} optionValue={value} optionKey={key} ownerState={props} onChange={onChange}/>
      ))}
      </div>
    </GInput>
  );
}



const InputHidden: React.FC<{ 
  id: string;
  value: string[] | null;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {
  const {id, value, onChange} = props;
  const newInputValue = value ?? [];
  const [inputValue, setInputValue] = React.useState(value)
  const ref = React.useRef<HTMLInputElement>(null); 

  React.useEffect(() => {
    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, value]);

  // trigger event on the hidden input that will contain technical date
  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  return (<input name={id} hidden value={newInputValue} ref={ref} onChange={() => {}} />);
}