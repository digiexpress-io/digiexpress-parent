import React from 'react'
import { Box, Typography, Grid2, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { useIntl } from 'react-intl'
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { InputFORMField } from './InputFORMField';

interface InputFORMProps {
  value: string;
  selectedDirent?: Fs.DirentBase;

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

const getValueFromJson = (parameter: Fs.TypeDef, json: Record<string, any>) => {
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

const InputFORM: React.FC<InputFORMProps> = ({ onSelect, onClose, value, selectedDirent }) => {
  const intl = useIntl();
  const { fetchDirentBody } = useFsDirent();
  const [json, setJson] = React.useState<object>(parseInput(value));
  const [typeDefs, setTypeDefs] = React.useState<Fs.TypeDef[]>([]);

  React.useEffect(() => {
    if (!selectedDirent) {
      return;
    }
    const { id, type } = selectedDirent;
    if (type === 'DECISION_TABLE') {
      fetchDirentBody(id, 'DECISION_TABLE')
        .then(body => {
          const defs = (body as Fs.WrenchBody).decisions[id]?.ast?.headers?.acceptDefs ?? [];
          setTypeDefs(defs.map(d => ({ id: d.id, name: d.name, order: d.order, data: false, direction: d.direction, valueType: d.valueType as Fs.ValueType, required: false, properties: [], valueSet: d.valueSet })));
        })
        .catch(() => setTypeDefs([]));
    } else if (type === 'FLOW_TASK') {
      fetchDirentBody(id, 'FLOW_TASK')
        .then(body => {
          const defs = (body as Fs.WrenchBody).services[id]?.ast?.headers?.acceptDefs ?? [];
          setTypeDefs(defs.map(d => ({ id: d.id, name: d.name, order: d.order, data: false, direction: d.direction, valueType: d.valueType as Fs.ValueType, required: false, properties: [], valueSet: d.valueSet })));
        })
        .catch(() => setTypeDefs([]));
    } else if (type === 'FLOW') {
      fetchDirentBody(id, 'FLOW')
        .then(body => {
          const defs = (body as Fs.WrenchBody).flows[id]?.ast?.headers?.acceptDefs ?? [];
          setTypeDefs(defs.map(d => ({ id: d.id, name: d.name, order: d.order, data: false, direction: d.direction, valueType: d.valueType as Fs.ValueType, required: false, properties: [], valueSet: d.valueSet })));
        })
        .catch(() => setTypeDefs([]));
    }
  }, [selectedDirent]);

  const handleChange = (newValue: string, typeDef: Fs.TypeDef) => {
    const newObject: Record<string, any> = {};
    newObject[typeDef.name] = newValue;
    setJson(Object.assign({}, json, newObject))
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'debug.input.form' })}</DialogTitle>
      <DialogContent>
        <Box><Typography variant="h4" fontWeight="bold">{intl.formatMessage({ id: 'debug.input.formTitle' })}</Typography></Box>
        {!selectedDirent ? (<Box><Typography variant="h4" fontWeight="bold">{intl.formatMessage({ id: 'debug.input.noAsset' })}</Typography></Box>) : null}
        {!selectedDirent ? null : (
          <Grid2 container spacing={2}>
            {typeDefs.map((typeDef, index) => (
              <Grid2 size={{ xs: 4 }} key={index}>
                <InputFORMField typeDef={typeDef} value={getValueFromJson(typeDef, json)} onChange={handleChange} />
              </Grid2>)
            )}
          </Grid2>
        )}
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={() => {
            onSelect(json);
            onClose();
          }}>
          {intl.formatMessage({ id: 'buttons.apply' })}
        </Button>
      </DialogActions>
    </Dialog>);
}

export type { InputFORMProps };
export { InputFORM };
