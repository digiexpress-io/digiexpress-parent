import React from 'react';
import { Dropdown } from 'semantic-ui-react';

const ChoiceProp = ({ onChange, value, name, item, options }) => {
  const opts = options.map(p => ({ key: p.key, text: p.label, value: p.key }));

  return (<Dropdown search selection fluid
    options={opts}
    value={value}
    onChange={(_, data) => onChange(data.value)} />);
};

export default ChoiceProp;
