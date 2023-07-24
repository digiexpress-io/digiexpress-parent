import React from 'react';
import { CheckboxProps, Checkbox, Theme } from '@mui/material';
import { SxProps } from '@mui/system';
import { styled } from "@mui/material/styles";



const StyledCheckboxRoot = styled(Checkbox)<CheckboxProps>(({ theme }) => ({
  color: theme.palette.uiElements.main,
  '&.Mui-checked': {
    color: theme.palette.uiElements.main,
  }
}))


const StyledCheckbox: React.FC<{
  checked: boolean;
  onChange?: (newValue: boolean) => void;
  sx?: SxProps<Theme>;
}> = (props) => {
  return (<StyledCheckboxRoot value={props.checked} sx={props.sx} onChange={({target}) => {
    if(props.onChange) {
      props.onChange(target.checked);
    }
  }} />)
}

export { StyledCheckbox }
