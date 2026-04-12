import React from 'react'
import { Alert, Grid2, TextField, Typography } from '@mui/material';

import { HdesApi } from '@dxs-ts/wrench-api';
import { InputFORMField } from './InputFORMField';
import { useFlowInput } from './useFlowInput';

export interface DebugFormProps {
  selected: HdesApi.EntityId;
  onChange: (json: object) => void;
}

export const DebugForm: React.FC<DebugFormProps> = ({ selected, onChange }) => {
  const { elements, input, setInput } = useFlowInput(selected);
  const [rawJson, setRawJson] = React.useState<string>('');
  const [jsonError, setJsonError] = React.useState<string | undefined>();

  const hasFlowInputs = elements.length > 0;

  React.useEffect(() => {
    onChange(input);
  }, [input, onChange]);

  const handleChange = (newValue: string, typeDef: HdesApi.TypeDef) => {
    const newObject: Record<string, any> = {};
    newObject[typeDef.name] = newValue;
    const nextJson = Object.assign({}, input, newObject);
    setInput(nextJson);
  }

  const handleRawJsonChange = (value: string) => {
    setRawJson(value);
    try {
      const parsed = JSON.parse(value);
      setJsonError(undefined);
      onChange(parsed);
    } catch {
      setJsonError('Invalid JSON');
    }
  }

  if (!hasFlowInputs) {
    return (
      <Grid2 container spacing={2} sx={{ minWidth: 500 }}>
        <Grid2 size={{ xs: 12 }}>
          <TextField
            multiline
            fullWidth
            minRows={16}
            maxRows={30}
            label='Document JSON (doc-data)'
            placeholder={'{\n  "metadata": { "date": "01.01.2026" },\n  "pages": []\n}'}
            value={rawJson}
            onChange={(e) => handleRawJsonChange(e.target.value)}
            error={!!jsonError}
            helperText={jsonError}
            slotProps={{ htmlInput: { style: { fontFamily: 'monospace', fontSize: 12 } } }}
          />
        </Grid2>
      </Grid2>
    );
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