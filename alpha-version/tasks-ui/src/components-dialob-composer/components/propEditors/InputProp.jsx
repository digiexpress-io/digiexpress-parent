import React from 'react';
import { Input } from "semantic-ui-react";

const InputProp = ({ onChange, value, name, item, ...props }) => {
  return (<Input transparent fluid onChange={(e) => onChange(e.target.value)} value={value || ''} {...props} />);
};

export default InputProp;
