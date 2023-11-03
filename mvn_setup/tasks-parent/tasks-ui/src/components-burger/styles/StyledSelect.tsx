import React from 'react';
import { SxProps } from '@mui/system';
import { styled } from "@mui/material/styles";
import { InputLabel, FormControl, MenuItem, Select, FormControlProps, FormHelperText, Theme } from '@mui/material';
import { FormattedMessage } from 'react-intl';



const StyledFormControl = styled(FormControl)<FormControlProps>(({ theme }) => ({
  marginTop: theme.spacing(2),
  color: theme.palette.uiElements.main,
  backgroundColor: theme.palette.background.paper,
  '& .MuiOutlinedInput-root': {
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.uiElements.main,
    },
  }
}));


interface StyledSelectProps<T> {
  label: string;
  items: { id: string, value: string | React.ReactChild, sx?: SxProps<Theme> }[];
  selected: T;
  disabled?: boolean;
  helperText?: string;
  empty?: { id: string, label: string }
  onChange: (values: T) => void;
}


const StyledSelect: React.FC<StyledSelectProps<string>> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <StyledFormControl variant="outlined" fullWidth>
      <InputLabel>{title}</InputLabel>
      <Select
        value={props.selected}
        disabled={props.disabled}
        onChange={({ target }) => props.onChange(target.value as any)}
        label={title}>

        {props.empty ? <MenuItem value={props.empty.id}><FormattedMessage id={props.empty.label} /></MenuItem> : null}
        {props.items.map(item => (<MenuItem key={item.id} value={item.id} sx={item.sx}>{item.value}</MenuItem>))}
      </Select>
      {props.helperText ? <FormHelperText><FormattedMessage id={props.helperText} /></FormHelperText> : null}
    </StyledFormControl>
  );
}

const StyledSelectMultiple: React.FC<{
  multiline?: boolean;
  open?: boolean;
  helpers?: { id: string, value: string | React.ReactChild, sx?: SxProps<Theme> }[];
  renderValue?: (values: string[]) => React.ReactNode;
} & StyledSelectProps<string[]>> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <StyledFormControl variant="outlined" fullWidth>
      <InputLabel>{title}</InputLabel>
      <Select
        multiple={true}
        multiline={props.multiline}
        disabled={props.disabled}
        value={props.selected}
        
        onChange={({ target }) => props.onChange((target.value as string[]).filter(id => !id.startsWith("_helpers_")))}
        renderValue={props.renderValue}
        label={title}>

        {props.helpers?.map((item, index) => (<MenuItem key={index} value={"_helpers_"+ index} sx={item.sx}>{item.value}</MenuItem>))}
        {props.empty ? <MenuItem value={props.empty.id}><FormattedMessage id={props.empty.label} /></MenuItem> : null}
        {props.items.map(item => (<MenuItem key={item.id} value={item.id} sx={item.sx}>{item.value}</MenuItem>))}
      </Select>
      {props.helperText ? <FormHelperText><FormattedMessage id={props.helperText} /></FormHelperText> : null}
    </StyledFormControl>
  );
}

export type { StyledSelectProps }
export { StyledSelect, StyledSelectMultiple }