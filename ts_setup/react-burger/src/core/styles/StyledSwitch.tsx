import React from 'react';
import { alpha, styled } from '@mui/material/styles';
import { Switch, FormControlLabel, FormHelperText } from '@mui/material';
import { FormattedMessage } from 'react-intl';




const StyledSwitchRoot = styled(Switch)(({ theme }) => ({
  '& .MuiSwitch-switchBase.Mui-checked': {
    color: theme.palette.uiElements.main,
    '&:hover': {
      backgroundColor: alpha(theme.palette.uiElements.main, 0.1),
    },
  },
  '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
    backgroundColor: alpha(theme.palette.uiElements.main, 0.5),
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


