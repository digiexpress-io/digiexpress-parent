import React from 'react';
import { Switch, FormControlLabel, FormHelperText } from '@mui/material';
import { FormattedMessage } from 'react-intl';





interface StyledSwitchProps {
  onChange: (newValue: boolean) => void,
  checked: boolean,
  label?: string,
  helperText?: string
}

const StyledSwitch: React.FC<StyledSwitchProps> = (props) => {

  const switchControl = <Switch
    onChange={(event) => props.onChange(event.target.checked)}
    checked={props.checked}
  />
  if(!props.label) {
    return switchControl;
  }

  return (
    <>
      <FormControlLabel
        sx={{ mt: 2 }}
        control={switchControl}
        label={<FormattedMessage id={props.label} />} />

      {  props.helperText ? (<FormHelperText>
        <FormattedMessage id={props.helperText} />
      </FormHelperText>) : null
      }
    </>
  )
}

export type { StyledSwitchProps }
export { StyledSwitch }


