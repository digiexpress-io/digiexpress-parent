import React from 'react'
import { Box, FormControlLabel, Radio, SelectChangeEvent, Typography } from '@mui/material'
import { RadioButtonChecked as RadioButtonCheckedIcon, RadioButtonUnchecked as RadioButtonUncheckedIcon } from '@mui/icons-material'


import { GInputRadioGroup, useUtilityClasses } from './useUtilityClasses';
import { GInputListProps } from './g-input-list-types';


export const ReadOnlyRadio: React.FC<GInputListProps> = (props) => {
  const { datasource, value: selectedValue } = props;
  const classes = useUtilityClasses(props.id, props.variant);

  if (!datasource) {
    return <>valueset is not defined</>;
  }

  return (
    <div className={classes.input}>
      {datasource.entries.map(({ key, value }) => {
        const selected = key + '' === selectedValue + '';
        return (
          <Box key={key} className={classes.option}>
            {selected ? <RadioButtonCheckedIcon /> : <RadioButtonUncheckedIcon />}
            <Typography>{value}</Typography>
          </Box>
        );
      })}
    </div>
  );
}


export const GInputRadio: React.FC<GInputListProps> = (props) => {
  const { datasource, onChange } = props;
  const classes = useUtilityClasses(props.id, props.variant);
  
  function handleChange(selectEvent: SelectChangeEvent) {
    const event: React.ChangeEvent<HTMLInputElement> = selectEvent as React.ChangeEvent<HTMLInputElement>;
    onChange(event);
  }
  const { value: selectedValue } = props;
  if (!datasource) {
    return (<>
      valueset is not defined
    </>);
  }
  return (
    <GInputRadioGroup className={classes.input} onChange={handleChange} value={selectedValue}>

      {/** All selection from data source */}
      {datasource.entries.map(({ key, value }) => (<FormControlLabel disabled={props.disabled} key={key} value={key} control={<Radio />} label={value} />))}

    </GInputRadioGroup>
  );
}