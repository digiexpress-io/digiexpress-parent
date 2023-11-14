import React from 'react';
import { Checkbox } from "semantic-ui-react";

const BoolProp = ({ onChange, value, name, item, ...props }) => {
  return (<Checkbox toggle onChange={(_, data) => onChange(data.checked)} checked={!!value} {...props} />);
};

export default BoolProp;
