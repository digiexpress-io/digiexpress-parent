import React from "react";
import { ListItemText } from "@mui/material";
import Burger from "@the-wrench-io/react-burger";
import { Client } from "../../context";
import { FormattedMessage } from "react-intl";

interface InputFORMFieldProps {
  typeDef: Client.TypeDef;
  value: string;
  onChange: (newValue: string, typeDef: Client.TypeDef) => void;
}

const validateNumberRange = (typeDef: Client.TypeDef, value: string) => {
  if (typeDef.values) {
    const [min, max] = typeDef.values.split(" - ");
    if (Number(value) < Number(min) || Number(value) > Number(max)) {
      return "debug.input.form.invalid.range";
    } else {
      return undefined;
    } 
  }
}

const validateNumberType = (typeDef: Client.TypeDef, value: string) => {
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
    return (<Burger.Select label={typeDef.name}
      selected={value}
      onChange={(newValue) => onChange(newValue, typeDef)}
      empty={{ id: '', label: 'noValue' }}
      items={[{ text: "true" }, { text: "false" }].map((type) => ({
        id: type.text,
        value: (<ListItemText primary={type.text} />)
      }))}
    />);
  }

  if (typeDef.valueType === 'STRING' && typeDef.values && typeDef.values.includes(", ")) {
    return (<Burger.Select label={typeDef.name}
      selected={value}
      onChange={(newValue) => onChange(newValue, typeDef)}
      empty={{ id: '', label: 'noValue' }}
      items={typeDef.values.split(", ").map((type) => ({
        id: type,
        value: (<ListItemText primary={type} />)
      }))}
    />);
  }

  if ((typeDef.valueType === 'INTEGER' || typeDef.valueType === 'LONG' || typeDef.valueType === 'DECIMAL') && typeDef.values && typeDef.values.includes(" - ")) {
    return (<Burger.TextField
      error={error !== undefined}
      onChange={(newValue) => {
        onChange(newValue, typeDef);
        if (newValue !== "") {
          setError(validateNumberType(typeDef, newValue));
        } else {
          setError(undefined);
        } 
      }}
      required={typeDef.required}
      label={typeDef.name}
      value={value}
      helperText={error ? error : typeDef.values}
      />);
  }
  
  return (<Burger.TextField
    onChange={(newValue) => onChange(newValue, typeDef)}
    required={typeDef.required}
    label={typeDef.name}
    value={value} /> 
  )
};

export { InputFORMField }
