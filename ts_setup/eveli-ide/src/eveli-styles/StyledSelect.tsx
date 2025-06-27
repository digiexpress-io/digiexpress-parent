import React from 'react';
import { InputLabel, FormControl, MenuItem, Select, FormHelperText, Theme, SxProps, styled, FormControlProps, ListItemText, SelectChangeEvent, Checkbox } from '@mui/material';
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
  marginBottom: theme.spacing(1),
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

  const SELECT_ALL_ID = "_select_all_";
  const DESELECT_ALL_ID = "_deselect_all_";

  const allItemIds = props.items.map(item => item.id);

  const handleChange = (event: SelectChangeEvent<string[]>) => {
    const value = event.target.value;
    const newValues = typeof value === 'string' ? value.split(',') : value;

    if (newValues.includes(SELECT_ALL_ID)) {
      const isAllSelected = allItemIds.every(id => props.selected.includes(id));
      props.onChange(isAllSelected ? [] : allItemIds);
      return;
    } else if (newValues.includes(DESELECT_ALL_ID)) {
      props.onChange([]);
    } else {
      props.onChange(newValues.filter(id => !id.startsWith("_helpers_")));
    }
  };



  return (
    <FormControl variant="outlined" fullWidth>
      <InputLabel>{title}</InputLabel>
      <Select 
        multiple={true}
        multiline={props.multiline}
        disabled={props.disabled}
        value={props.selected}
        
        onChange={handleChange}
        renderValue={props.renderValue}
        label={title}>
        <MenuItem value={SELECT_ALL_ID}>
          <Checkbox checked={props.selected.length === allItemIds.length} />
          <ListItemText primary={<FormattedMessage id="select.all" defaultMessage="Apply to all articles" />} />
        </MenuItem>
        <MenuItem value={DESELECT_ALL_ID}>
          <Checkbox checked={props.selected.length === 0} />
          <ListItemText primary={<FormattedMessage id="deselect.all" defaultMessage="Do not apply to any articles" />} />
        </MenuItem>

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