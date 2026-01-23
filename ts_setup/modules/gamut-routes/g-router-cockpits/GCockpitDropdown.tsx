import React from 'react'
import { SelectChangeEvent } from '@mui/material';
import { Check as CheckIcon } from '@mui/icons-material';
import { useIam, useSite } from '@dxs-ts/gamut-api';
import { useGTheme } from '@dxs-ts/gamut-theme';
import { Cockpit } from '@dxs-ts/gamut-cockpit-store';

import { GInputSelect, GInputSelectOption, useUtilityClasses } from './useUtilityClasses';



const DEFAULT_SELECTION_VALUE = 'System default';

export const GCockpitDropdown: React.FC<{}> = (props) => {
  const { cockpits } = useSite();  
  const iam = useIam();
  const classes = useUtilityClasses();
  const gTheme = useGTheme();
  
  const datasource = cockpits.options;
  const selectedValue = cockpits.active;

  async function handleChange(selectEvent: SelectChangeEvent) {
    const event: React.ChangeEvent<HTMLInputElement> = selectEvent as React.ChangeEvent<HTMLInputElement>;
    const selected = event.target.value;
    const found = datasource.find(item => item.id === selected);
    cockpits.setActive(found);
    await iam.reload();

    // switch the theme if possible
    gTheme.setThemeOptions(found?.cockpitConfigName);
  }

  const value = selectedValue?.id ?? DEFAULT_SELECTION_VALUE;

  return (
    <GInputSelect
      className={classes.input}
      onChange={handleChange}
      renderValue={(selected: string) => <Collapsed datasource={datasource} selected={selected} className={classes.collapsed} />}
      value={value}>

      <GInputSelectOption value={DEFAULT_SELECTION_VALUE}>
        <div className={classes.optionValue}>{DEFAULT_SELECTION_VALUE}</div>
        <div className={classes.optionChecked}>{!value || value === DEFAULT_SELECTION_VALUE ? <CheckIcon /> : <></>}</div>
      </GInputSelectOption>

      {/** All selection from data source */}
      {datasource.map((option) => {
        const { id, cockpitConfigName, cockpitConfigDescription } = option;
        const selected = id === selectedValue?.id;
        const prefix = selected ? <CheckIcon /> : null;

        return (<GInputSelectOption key={id} value={id} className={classes.option}>
          {<div className={classes.optionKey}>{cockpitConfigName}</div>}
          <div className={classes.optionValue}>{cockpitConfigDescription}</div>
          <div className={classes.optionChecked}>{prefix}</div>
        </GInputSelectOption>);
      })}

    </GInputSelect>
  );
}

const Collapsed: React.FC<{
  datasource: Cockpit[];
  selected: string;
  className: string;
}> = ({ datasource, selected, className }) => {

  const selectedItem = datasource.find(item => item.id === selected);
  
  if (selectedItem) {
    return (
      <div className={className}>
        <div>{selectedItem.cockpitConfigName}</div>
        <div>{selectedItem.cockpitConfigDescription}</div>
      </div>
    );
  }
  return (
    <div className={className}>
      <div className={className}>{DEFAULT_SELECTION_VALUE}</div>
    </div>
  );
}