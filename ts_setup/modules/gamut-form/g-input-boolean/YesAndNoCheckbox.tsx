import React from 'react';
import { CheckBox as CheckBoxIcon, CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon  } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { GInputBaseAnyProps } from "../g-input-base";
import { GInputBooleanProps } from "./GInputBoolean";
import { useUtilityClasses } from './useUtilityClasses';
import { Button, Typography } from '@mui/material';



export const YesAndNoCheckbox: React.FC<GInputBaseAnyProps & GInputBooleanProps> = (props) => {
  const { onChange, id, variant, value } = props;
  const ref = React.useRef<HTMLInputElement>(null);
  const classes = useUtilityClasses(id, variant);
  const [inputValue, setInputValue] = React.useState<string>(value === undefined || value === null ? '' : value + '');
  const [sync, setSync] = React.useState<boolean>(false);

  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  React.useEffect(() => {
    if(sync) {
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  },[sync, inputValue]);

  function toggleYes() {
    setInputValue(inputValue === 'true' ? '' : 'true');
    setSync(true);
  }
  function toggleNo() {
    setInputValue(inputValue === 'false' ? '' : 'false');
    setSync(true);

  }

  function doNothing() {

  }

  function startIcon(checked: boolean) {
    return checked ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />;
  }

  const isYes: boolean = inputValue === 'true';
  const isNo: boolean = inputValue === 'false';

  return (
    <div className={classes.input}>
      <Button disabled={props.disabled} fullWidth className={classes.option} variant='outlined' onClick={toggleYes} startIcon={startIcon(isYes)}>
        <Typography className={classes.optionTitle}><FormattedMessage id='gamut.forms.answer.boolean.yes'/></Typography>
      </Button>
      
      <Button disabled={props.disabled} fullWidth className={classes.option} variant='outlined' onClick={toggleNo} startIcon={startIcon(isNo)}>
        <Typography className={classes.optionTitle}><FormattedMessage id='gamut.forms.answer.boolean.no'/></Typography>
      </Button>
      
      <input hidden value={inputValue} ref={ref} onChange={doNothing} />
    </div>
  );
}