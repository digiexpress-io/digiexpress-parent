import React from 'react'
import { Grid2 } from '@mui/material';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { InputFORMField } from './InputFORMField';


export interface DebugFormProps {  
  selected: HdesApi.EntityId;
  onChange: (json: object) => void;
}

export const DebugForm: React.FC<DebugFormProps> = ({ selected, onChange }) => {
  const { decisions, flows, services } = Composer.useSite();
  const [json, setJson] = React.useState<object>({});

  const asset: HdesApi.Entity<HdesApi.AstBody> | undefined = React.useMemo(() => {
    if (flows[selected]) {
      return flows[selected];
    }
    return Object.values(flows).find(flow => flow.ast?.name === selected);
  }, [selected, flows, services, decisions]);


  const handleChange = (newValue: string, typeDef: HdesApi.TypeDef) => {
    const newObject: Record<string, any> = {};
    newObject[typeDef.name] = newValue;
    const nextJson: object = Object.assign({}, json, newObject);
    setJson(nextJson);
    onChange(nextJson);
  }
  const elements = asset?.ast ? asset.ast.headers.acceptDefs : [];

  return (
    <Grid2 container spacing={2}>
      {elements.map((typeDef, index) => (
        <Grid2 size={{ xs: 4 }} key={index}>
          <InputFORMField typeDef={typeDef} value={getValueFromJson(typeDef, json)} onChange={handleChange} />
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