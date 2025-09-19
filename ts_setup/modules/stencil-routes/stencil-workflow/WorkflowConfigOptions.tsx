import React from 'react';
import { Box, Typography, Grid2, Chip, ListItemText, Checkbox } from '@mui/material';

import { useIntl } from 'react-intl';
import * as Burger from '@dxs-ts/eveli-primitives';


interface WorkflowIntl {
  value: WorkflowFeatureType;
  label: string,
  helperText: string;
}

type WorkflowFeatureType = 'DEV' | 'ANON' | 'DISABLED' | 'ASSIGNABLE' | 'NONE';

const all_features: WorkflowIntl[] = [
  {
    value: 'DEV',
    label: 'services.devmode.label',
    helperText: 'services.devmode.helper'
  },
  {
    value: 'ANON',
    label: 'services.anonmode.label',
    helperText: 'services.anonmode.helper'
  },
  {
    value: 'DISABLED',
    label: 'services.disabledmode.label',
    helperText: 'services.disabledmode.helper'
  },
  {
    value: 'ASSIGNABLE',
    label: 'services.assignableMode.label',
    helperText: 'services.assignableMode.helper'
  },
  {
    value: 'NONE',
    label: 'services.none.label',
    helperText: 'services.none.helper'
  },
];

function getIntl(type: string) {
  return all_features.find(({ value }) => value === type)!;
}

function getTypes(value: WorkflowOptions): WorkflowFeatureType[] {

  const result: WorkflowFeatureType[] = [];
  if (value.anon === true) {
    result.push('ANON');
  }

  if (value.disabled === true) {
    result.push('DISABLED');
  }

  if (value.assignable === true) {
    result.push('ASSIGNABLE');
  }

  if (value.devMode === true) {
    result.push('DEV');
  }

  return result;
}


export type WorkflowOptions = {
  devMode: boolean | undefined,
  anon: boolean | undefined,
  disabled: boolean | undefined,
  assignable: boolean | undefined,
}

export const WorkflowConfigOptions: React.FC<{ onChange: (props: WorkflowOptions) => void, value: WorkflowOptions }> = ({ value, onChange }) => {
  const intl = useIntl();
  const selectedModes: WorkflowFeatureType[] = getTypes(value);

  const handleChange = (selected: string[]) => {
    const newState = selected as WorkflowFeatureType[];
    onChange({
      anon: newState.includes('ANON') ? true : undefined,
      devMode: newState.includes('DEV') ? true : undefined,
      disabled: newState.includes('DISABLED') ? true : undefined,
      assignable: newState.includes('ASSIGNABLE') ? true : undefined
    });
  }
  const none = getIntl('NONE');

  return (<>
    <Burger.InputLabel>{intl.formatMessage({ id: 'services.modeselect.title' })}</Burger.InputLabel>

    <Burger.SelectMultiple label='services.modeselect.label'
      multiline
      selected={selectedModes}
      onChange={handleChange}
      renderValue={(selected) => (
        <Box display='flex' gap={1}>
          {selected
            .map(getIntl)
            .map(({ value, label }) => <Chip key={value} label={intl.formatMessage({ id: label })} />)}
        </Box>
      )}
      items={all_features.filter(({ value }) => value !== 'NONE').map((article) => ({
        id: article.value,
        value: (<>
          <Checkbox checked={selectedModes.includes(article.value)} />
          <ListItemText primary={intl.formatMessage({ id: article.label })} />
        </>)
      }))}
    />

    <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
      {selectedModes.length === 0 &&
        (<Typography variant='caption' component='div'>
          {intl.formatMessage({ id: none.label })}
        </Typography>)
      }

      {selectedModes.map(getIntl).map(({ helperText, value }) => (
        <Typography key={value} variant='caption' component='div'>
          {intl.formatMessage({ id: helperText })}
        </Typography>
      ))}
    </Grid2>
  </>)
}