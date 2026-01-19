import React from 'react'
import { SelectChangeEvent } from '@mui/material';
import { Check as CheckIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { SiteApi, useIam, useSite } from '@dxs-ts/gamut-api';


import { GInputSelect, GInputSelectOption, useUtilityClasses } from './useUtilityClasses';


const UNDEFINED_SELECTION_VALUE = 'gamut.forms.selectionUndefined';

export const GCockpitDropdown: React.FC<{}> = (props) => {
  const { cockpits } = useSite();  
  const iam = useIam();
  const classes = useUtilityClasses();
  const intl = useIntl();
  const undefinedValue = intl.formatMessage({ id: UNDEFINED_SELECTION_VALUE });
  
  const datasource = cockpits.options;
  const selectedValue = cockpits.active;

  async function handleChange(selectEvent: SelectChangeEvent) {
    const event: React.ChangeEvent<HTMLInputElement> = selectEvent as React.ChangeEvent<HTMLInputElement>;
    const selected = event.currentTarget.value;
    cockpits.setActive(datasource.find(item => item.id === selected));
    await iam.reload();
  }

  return (
    <GInputSelect
      className={classes.input}
      onChange={handleChange}
      renderValue={(selected: string) => <Collapsed datasource={datasource} selected={selected} className={classes.collapsed} />}
      value={selectedValue?.id}>

      <GInputSelectOption key={undefinedValue} value={undefinedValue}>{intl.formatMessage({ id: 'gamut.buttons.select' })}</GInputSelectOption>

      {/** All selection from data source */}
      {datasource.map(({ id, name, description }) => {
        const selected = id === selectedValue?.id;
        const prefix = selected ? <CheckIcon /> : null;
        return (<GInputSelectOption key={id} value={id} className={classes.option}>
          {<div className={classes.optionKey}>{name}</div>}
          <div className={classes.optionValue}>{description}</div>
          <div className={classes.optionChecked}>{prefix}</div>
        </GInputSelectOption>);
      })}

    </GInputSelect>
  );
}

const Collapsed: React.FC<{
  datasource: SiteApi.Cockpit[];
  selected: string;
  className: string;
}> = ({ datasource, selected, className }) => {

  const intl = useIntl();
  const selectedItem = datasource.find(item => item.id === selected);
  if (!selectedItem) {
    return <div className={className}>{intl.formatMessage({ id: 'gamut.buttons.select' })}</div>;
  }

  return (
    <div className={className}>
      <div>{selectedItem.name}</div>
      <div>{selectedItem.description}</div>
    </div>
  );
}