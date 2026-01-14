import React from 'react'
import { Grid2 } from '@mui/material';

import { HdesApi } from '@dxs-ts/wrench-api';
import { InputFORMField } from './InputFORMField';
import { useGetFlowInput } from './useGetFlowInput';

export interface DebugFormProps {
  selected: HdesApi.EntityId;
  onChange: (json: object) => void;
}

export const DebugForm: React.FC<DebugFormProps> = ({ selected, onChange }) => {
  const { elements, input, setInput } = useGetFlowInput(selected);

  React.useEffect(() => {
    onChange(input);
  }, [input, onChange]);

  const handleChange = (newValue: string, typeDef: HdesApi.TypeDef) => {
    const newObject: Record<string, any> = {};
    newObject[typeDef.name] = newValue;
    const nextJson = Object.assign({}, input, newObject);
    setInput(nextJson);
  }

  return (
    <Grid2 container spacing={2}>
      {elements.map((typeDef, index) => (
        <Grid2 size={{ xs: 4 }} key={index}>
          <InputFORMField typeDef={typeDef} value={getValueFromJson(typeDef, input)} onChange={handleChange} />
        </Grid2>)
      )}
    </Grid2>
  );
}

const getValueFromJson = (parameter: HdesApi.TypeDef, json: Record<string, any>) => {
  const init = json[parameter.name];
  if (init === undefined) {
    return parameter.values ? parameter.values : "";
  }
  if (init.includes(" - ")) {
    return init.split(" - ")[0];
  }
  if (init.includes(", ")) {
    return init.split(", ")[0];
  }
  return init;
}