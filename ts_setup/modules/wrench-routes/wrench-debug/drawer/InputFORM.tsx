import React from 'react'

import { Box, Typography, Grid2, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { FormattedMessage } from 'react-intl'


import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { InputFORMField } from './InputFORMField';
import { CancelButton } from '@dxs-ts/eveli-primitives';

interface InputFORMProps {
  value: string;
  selected?: HdesApi.EntityId;

  onClose: () => void;
  onSelect: (json: object) => void;
}

const parseInput = (json: string) => {
  try {
    var parsed = JSON.parse(json);
    for (var key in parsed) {
      if (parsed[key].includes(" - ")) {
        parsed[key] = parsed[key].split(" - ")[0];
      }
      if (parsed[key].includes(", ")) {
        parsed[key] = parsed[key].split(", ")[0];
      }
    }
    return parsed;
  } catch (e) {
    console.error(e);
    return {};
  }
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

const InputFORM: React.FC<InputFORMProps> = ({ onSelect, onClose, value, selected }) => {
  const { decisions, flows, services } = Composer.useSite();
  const [json, setJson] = React.useState<object>(parseInput(value));

  const asset: HdesApi.Entity<HdesApi.AstBody> | undefined = React.useMemo(() => {
    if (!selected) {
      return undefined;
    }
    if (decisions[selected]) {
      return decisions[selected];
    }
    if (flows[selected]) {
      return flows[selected];
    }
    if (services[selected]) {
      return services[selected];
    }
  }, [selected, flows, services, decisions]);


  const handleChange = (newValue: string, typeDef: HdesApi.TypeDef) => {
    const newObject: Record<string, any> = {};
    newObject[typeDef.name] = newValue;
    setJson(Object.assign({}, json, newObject))
  }
  const elements = asset?.ast ? asset.ast.headers.acceptDefs : [];

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='debug.input.form' /></DialogTitle>
    <DialogContent>
      <Box><Typography variant="h4" fontWeight="bold"><FormattedMessage id={"debug.input.formTitle"} /></Typography></Box>
      {!selected ? (<Box><Typography variant="h4" fontWeight="bold"><FormattedMessage id={"debug.input.noAsset"} /></Typography></Box>) : null}
      {!selected ? null : (
        <Grid2 container spacing={2}>
          {elements.map((typeDef, index) => (
            <Grid2 size={{ xs: 4 }} key={index}>
              <InputFORMField typeDef={typeDef} value={getValueFromJson(typeDef, json)} onChange={handleChange} />
            </Grid2>)
          )}
        </Grid2>
      )}
    </DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={() => {
          onSelect(json);
          onClose();
        }}>
        <FormattedMessage id='buttons.apply'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export type { InputFORMProps };
export { InputFORM };
