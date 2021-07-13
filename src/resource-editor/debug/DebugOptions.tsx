import React from 'react';
import { Autocomplete, TextField } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';

import Resource from '../';
import { Hdes } from '../deps';


interface DebugOption {
  label: string,
  asset: Hdes.ModelAPI.Model
}

interface DebugOptionsProps {
  onChange: (model: Hdes.ModelAPI.Model) => void;
};

const DebugOptions: React.FC<DebugOptionsProps> = ({onChange}) => {
  const resource = Resource.useContext();

  const keys: string[] = [];
  const assets = [
    ...resource.session.models.DT,
    ...resource.session.models.FLOW,
    ...resource.session.models.FLOW_TASK]
    .map(asset => {
      const label = keys.includes(asset.name) ? asset.name + " : " + asset.id : asset.name;
      keys.push(asset.name);

      return { label, asset };
    });

  const options: DebugOption[] = assets.sort((a, b) => {
    const type = b.asset.type.localeCompare(a.asset.type);
    if (type === 0) {
      return -b.asset.name.localeCompare(a.asset.name);
    }
    return type;
  });


  return (
    <Autocomplete fullWidth
      isOptionEqualToValue={(option: DebugOption, value: DebugOption) => option.asset.id === value.asset.id}
      onChange={(_event, entity) => onChange((entity as any).asset)}
      options={options}
      groupBy={(option) => option.asset.type}
      getOptionLabel={(option) => option.label}
      renderInput={(params) => <TextField {...params} label={<FormattedMessage id="debug.asset.select.label" />} variant="filled" />}
    />
  );
}

export type { DebugOptionsProps };
export { DebugOptions };
