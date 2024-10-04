import React from 'react'
import { OverridableStringUnion } from '@mui/types'
import { SelectChangeEvent, useThemeProps } from '@mui/material';
import CheckIcon from '@mui/icons-material/Check';
import { useIntl } from 'react-intl';

import { DialobApi } from '../api-dialob';
import { GInputBase, GInputBaseProps, LabelPosition } from '../g-input-base';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';


import { GInputListRoot, GInputSelect, GInputSelectOption, useUtilityClasses, MUI_NAME } from './useUtilityClasses';

// extension hook for adding custom input types
export interface GInputListPropsVariantOverrides { };


export interface GInputListProps {
  id: string;
  value: string;
  datasource: DialobApi.ActionValueSet;

  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  undefinedValue: string;
  keys?: boolean | undefined; // display keys

  variant: OverridableStringUnion<
    'list',
    GInputListPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'list',
    GInputListPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputListProps>;
}

export const GInputList: React.FC<GInputListProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })
  

  const {
    variant = 'list',
    keys = false,
    id, label, labelPosition, errors
  } = props;

  const ownerState = { ...props, variant, keys, name: id }
  
  const slots: GInputBaseProps<GInputListProps> = {
    id,
    slots: {
      error: GInputError, 
      label: GInputLabel,
      adornment: GInputAdornment,
      input: GInputDropdown,
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }

  const classes = useUtilityClasses(props.id, variant);


  return (<GInputListRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputListRoot>);
}




const GInputDropdown: React.FC<GInputListProps> = (props) => {
  const { datasource, onChange } = props;
  const classes = useUtilityClasses(props.id, props.variant);
  const intl = useIntl();

  function handleChange(selectEvent: SelectChangeEvent) {
    const event: React.ChangeEvent<HTMLInputElement> = selectEvent as React.ChangeEvent<HTMLInputElement>;
    onChange(event);
  }
  const { value: selectedValue, undefinedValue, keys } = props; 
  return (
    <GInputSelect
      className={classes.input}
      onChange={handleChange} 
      renderValue={(selected: string) => <Collapsed datasource={datasource} keys={props.keys} selected={selected} className={classes.collapsed} />}
      value={selectedValue}>


      <GInputSelectOption value={undefinedValue}>{intl.formatMessage({id: 'gamut.buttons.select'})}</GInputSelectOption>

      {/** All selection from data source */}
      {datasource.entries.map(({ key, value }) => {
        const selected = key === selectedValue;
        const prefix = selected ? <CheckIcon /> : null;
        return (<GInputSelectOption key={key} value={key} className={classes.option}>
            {keys && <div className={classes.optionKey}>{key}</div>}
            <div className={classes.optionValue}>{value}</div>
            <div className={classes.optionChecked}>{prefix}</div>
        </GInputSelectOption>);
      })}

    </GInputSelect>
  );
}

const Collapsed: React.FC<{
  datasource: DialobApi.ActionValueSet;
  selected: string;
  keys: boolean | undefined;
  className: string;
}> = ({ datasource, selected, className, keys }) => {
  const intl = useIntl();
  const selectedItem = datasource.entries.find(item => item.key + '' === selected + '');
  if (!selectedItem) {
    return <div className={className}>{intl.formatMessage({ id: 'gamut.buttons.select' })}</div>;
  }
  return (
    <div className={className}>
      <div>{keys && selectedItem.key}</div>
      <div>{selectedItem.value}</div>
    </div>
  );
}


