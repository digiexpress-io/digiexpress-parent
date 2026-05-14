import React from "react";
import { Typography, FormHelperText } from "@mui/material";
import { FsDirentSelectSingle } from '../../fs-dirent-select-single';
import { FsDirentTextField } from '../../fs-dirent-text-field';
import { Fs } from '@dxs-ts/fs-api';

interface InputFORMFieldProps {
  typeDef: Fs.TypeDef;
  value: string;
  onChange: (newValue: string, typeDef: Fs.TypeDef) => void;
}

const validateNumberRange = (typeDef: Fs.TypeDef, value: string) => {
  if (typeDef.values) {
    const [min, max] = typeDef.values.split(" - ");
    if (Number(value) < Number(min) || Number(value) > Number(max)) {
      return "debug.input.form.invalid.range";
    } else {
      return undefined;
    }
  }
}

const validateNumberType = (typeDef: Fs.TypeDef, value: string) => {
  if (typeDef.valueType === "INTEGER") {
    if (!Number.isInteger(Number(value))) {
      return "debug.input.form.invalid.integer";
    } else {
      return validateNumberRange(typeDef, value);
    }
  } else if (typeDef.valueType === "DECIMAL" || typeDef.valueType === "LONG") {
    if (isNaN(Number(value))) {
      return "debug.input.form.invalid.number";
    } else {
      return validateNumberRange(typeDef, value);
    }
  } else {
    return validateNumberRange(typeDef, value);
  }
}

const InputFORMField: React.FC<InputFORMFieldProps> = ({
  typeDef,
  value,
  onChange
}) => {

  const [error, setError] = React.useState<string | undefined>(undefined);

  if (typeDef.valueType === 'BOOLEAN') {
    return (<>
      <Typography variant="caption">{typeDef.name}</Typography>
      <FsDirentSelectSingle
        value={value}
        onChange={(newValue) => onChange(newValue, typeDef)}
        options={[
          { value: 'true', label: 'true' },
          { value: 'false', label: 'false' }
        ]}
      />
    </>);
  }

  if (typeDef.valueType === 'STRING' && typeDef.valueSet && typeDef.valueSet.length > 0) {
    return (<>
      <Typography variant="caption">{typeDef.name}</Typography>
      <FsDirentSelectSingle
        value={value}
        onChange={(newValue) => onChange(newValue, typeDef)}
        options={typeDef.valueSet.map((type) => ({
          value: type,
          label: type
        }))}
      />
    </>);
  }

  if ((typeDef.valueType === 'INTEGER' || typeDef.valueType === 'LONG' || typeDef.valueType === 'DECIMAL') && typeDef.values && typeDef.values.includes(" - ")) {
    return (<>
      <Typography variant="caption">{typeDef.name}</Typography>
      <FsDirentTextField
        required={typeDef.required}
        value={value}
        onChange={(newValue) => {
          onChange(newValue, typeDef);
          if (newValue !== "") {
            setError(validateNumberType(typeDef, newValue));
          } else {
            setError(undefined);
          }
        }}
      />
      {error && <FormHelperText error>{error}</FormHelperText>}
      {!error && typeDef.values && <FormHelperText>{typeDef.values}</FormHelperText>}
    </>);
  }

  return (<>
    <Typography variant="caption">{typeDef.name}</Typography>
    <FsDirentTextField
      required={typeDef.required}
      value={value}
      onChange={(newValue) => onChange(newValue, typeDef)}
    />
  </>);
};

export { InputFORMField }
