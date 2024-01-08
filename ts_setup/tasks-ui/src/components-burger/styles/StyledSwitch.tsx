import React from 'react';
import { alpha, styled } from '@mui/material/styles';
import { Switch, FormControlLabel, FormHelperText } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { cyan } from 'components-colors';




const StyledSwitchRoot = styled(Switch)(() => ({
  '& .MuiSwitch-switchBase.Mui-checked': {
    color: cyan,
    '&:hover': {
      backgroundColor: alpha(cyan, 0.1),
    },
  },
  '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
    backgroundColor: alpha(cyan, 0.5),
  },

}));

interface StyledSwitchProps {
  onChange: (newValue: boolean) => void,
  checked: boolean,
  label?: string,
  helperText?: string
}

const StyledSwitch: React.FC<StyledSwitchProps> = (props) => {

  const switchControl = <StyledSwitchRoot
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


