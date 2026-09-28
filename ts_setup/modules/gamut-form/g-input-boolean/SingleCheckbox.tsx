import React from 'react';
import { Button, Typography } from '@mui/material';
import { CheckBox as CheckBoxIcon } from '@mui/icons-material';
import { CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon } from '@mui/icons-material';

import { GInputBaseAnyProps } from '../g-input-base';
import { GInputBooleanProps } from './GInputBoolean';
import { useUtilityClasses } from './useUtilityClasses';


export const ReadOnlySingleCheckbox: React.FC<GInputBaseAnyProps & GInputBooleanProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const isChecked = props.value === true;

  return (
    <div className={classes.input}>
      <Button disabled fullWidth className={classes.option} variant='outlined'
        startIcon={isChecked ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />}>
        <Typography className={classes.singleCheckboxTitle}>{props.label}</Typography>
      </Button>
    </div>
  );
}


export const SingleCheckbox: React.FC<GInputBaseAnyProps & GInputBooleanProps> = (props) => {
  const { id, variant, value, disabled, label } = props;
  const classes = useUtilityClasses(id, variant);
  const ref = React.useRef<HTMLInputElement>(null);
  const [inputValue, setInputValue] = React.useState<string>(value === undefined ? '' : value + '');
  const [sync, setSync] = React.useState<boolean>(false);

  React.useEffect(() => {
    function populateTheChange(event: any) {
      props.onChange(event);
    }
    ref.current?.addEventListener('input', populateTheChange);
    return () => ref.current?.removeEventListener('input', populateTheChange);
  }, [props.onChange]);

  React.useEffect(() => {
    if (sync) {
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [sync, inputValue]);

  function handleToggle() {
    setInputValue(inputValue === 'true' ? 'false' : 'true');
    setSync(true);
  }

  function doNothing() {}

  function startIcon() {
    return inputValue === 'true'
      ? <CheckBoxIcon className={classes.optionIcon} />
      : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />;
  }

  return (
    <div className={classes.input}>
      <Button disabled={disabled} fullWidth className={classes.option} variant='outlined' onClick={handleToggle} startIcon={startIcon()}>
        <Typography className={classes.singleCheckboxTitle}>{label}</Typography>
      </Button>
      <input hidden value={inputValue} ref={ref} onChange={doNothing} />
    </div>
  );
}
