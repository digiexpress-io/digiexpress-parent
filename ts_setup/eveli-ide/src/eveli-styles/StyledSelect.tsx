import React from 'react';
import { InputLabel, FormControl, MenuItem, Select, FormHelperText, Theme, SxProps, styled, FormControlProps } from '@mui/material';
import { FormattedMessage } from 'react-intl';



interface StyledSelectProps<T> {
  label: string;
  items: { id: string, value: string | React.ReactNode, sx?: SxProps<Theme> }[];
  selected: T;
  disabled?: boolean;
  helperText?: string;
  empty?: { id: string, label: string }
  onChange: (values: T) => void;
}

const StyledSelectLabel = styled(InputLabel)(({ theme }) => ({
  marginTop: theme.spacing(2),
  color: theme.palette.text.primary,
  fontWeight: theme.typography.h1.fontWeight,
  paddingLeft: theme.spacing(2),
}));

const StyledSelectFormControl = styled(FormControl)<FormControlProps>(({ theme }) => ({
  marginTop: theme.spacing(1),
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.background.paper,
}));

const StyledSelectHelperText = styled(FormHelperText)(({ theme }) => ({
  marginTop: theme.spacing(1),
  paddingLeft: theme.spacing(2),
}));

const StyledSelectBase = styled(Select)(({ theme }) => ({
  height: '54.5px',
  display: 'flex',
  alignItems: 'center',
  boxSizing: 'border-box',
  padding: '0 14px',
}));


const StyledSelect: React.FC<StyledSelectProps<string>> = (props) => {
  const labelId = `${props.label}-label`;
  const title = <FormattedMessage id={props.label} />;

  return (
    <>
      <StyledSelectLabel id={labelId}>{title}</StyledSelectLabel>
      <StyledSelectFormControl variant="outlined" fullWidth>
        <StyledSelectBase
          labelId={labelId}
          id={`${props.label}-select`}
          value={props.selected}
          disabled={props.disabled}
          onChange={({ target }) => props.onChange(target.value as any)}
        >
          {props.empty && (
            <MenuItem value={props.empty.id}>
              <FormattedMessage id={props.empty.label} />
            </MenuItem>
          )}
          {props.items.map(item => (
            <MenuItem key={item.id} value={item.id} sx={item.sx}>
              {item.value}
            </MenuItem>
          ))}
        </StyledSelectBase>
        {props.helperText && (
          <StyledSelectHelperText>
            <FormattedMessage id={props.helperText} />
          </StyledSelectHelperText>
        )}
      </StyledSelectFormControl>
    </>
  );
};

const StyledSelectMultiple: React.FC<{
  multiline?: boolean;
  open?: boolean;
  helpers?: { id: string, value: string | React.ReactNode, sx?: SxProps<Theme> }[];
  renderValue?: (values: string[]) => React.ReactNode;
} & StyledSelectProps<string[]>> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <FormControl variant="outlined" fullWidth>
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
    </FormControl>
  );
}

export type { StyledSelectProps }
export { StyledSelect, StyledSelectMultiple }