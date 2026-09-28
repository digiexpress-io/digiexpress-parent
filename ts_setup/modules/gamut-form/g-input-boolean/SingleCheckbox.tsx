import React from 'react';
import { Checkbox } from '@mui/material';
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
      <div className={classes.option}>
        {isChecked
          ? <CheckBoxIcon className={classes.optionIcon} />
          : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />
        }
      </div>
    </div>
  );
}


export const SingleCheckbox: React.FC<GInputBaseAnyProps & GInputBooleanProps> = (props) => {
  const { id, variant, value, disabled } = props;
  const classes = useUtilityClasses(id, variant);
  const ref = React.useRef<HTMLInputElement>(null);
  const [inputValue, setInputValue] = React.useState<string>(value === undefined || value === null ? 'false' : value + '');
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

  function handleChange() {
    setInputValue(inputValue === 'true' ? 'false' : 'true');
    setSync(true);
  }

  function doNothing() {}

  const isChecked = inputValue === 'true';

  return (
    <div className={classes.input}>
      <div className={classes.option}>
        <Checkbox
          disabled={disabled}
          checked={isChecked}
          onChange={handleChange}
          className={classes.optionIcon}
        />
      </div>
      <input hidden value={inputValue} ref={ref} onChange={doNothing} />
    </div>
  );
}
