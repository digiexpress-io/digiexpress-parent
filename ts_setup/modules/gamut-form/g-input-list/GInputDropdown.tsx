import React from 'react'
import { SelectChangeEvent, TextField } from '@mui/material';
import { Check as CheckIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { DialobApi } from '@dxs-ts/gamut-api';
import { GInputSelect, GInputSelectOption, useUtilityClasses } from './useUtilityClasses';
import { GInputListProps } from './g-input-list-types';


export const GInputDropdown: React.FC<GInputListProps> = (props) => {
  const { datasource, onChange } = props;
  const classes = useUtilityClasses(props.id, props.variant);
  const intl = useIntl();

  function handleChange(selectEvent: SelectChangeEvent) {
    const event: React.ChangeEvent<HTMLInputElement> = selectEvent as React.ChangeEvent<HTMLInputElement>;
    onChange(event);
  }
  const { value: selectedValue, undefinedValue, keys } = props;
  if (!datasource) {
    return (<>
      valueset is not defined
    </>);
  }
  return (
    <GInputSelect
      className={classes.input}
      disabled={props.disabled}
      onChange={handleChange}
      renderValue={(selected: string) => <Collapsed datasource={datasource} keys={props.keys} selected={selected} className={classes.collapsed} />}
      value={selectedValue}>


      <GInputSelectOption key={undefinedValue} value={undefinedValue}>{intl.formatMessage({ id: 'gamut.buttons.select' })}</GInputSelectOption>

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

export const ReadOnlyDropdown: React.FC<GInputListProps> = (props) => {
  const { datasource, value } = props;
  const classes = useUtilityClasses(props.id, props.variant);
  const selectedItem = datasource?.entries.find(e => e.key + '' === value + '');
  const displayValue = selectedItem?.value ?? '--';

  return (
    <TextField fullWidth value={displayValue} className={classes.input} slotProps={{ input: { readOnly: true } }} />
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


