import React from 'react';
import { Dropdown } from 'semantic-ui-react';


const MultiChoiceProp = ({ onChange, value, name, item, options, allowAdditions }) => {
  let opts = options.map(p => ({ key: p.key, text: p.label, value: p.key }));
  if (Array.isArray(value)) {
    value.forEach(v => {
      if (opts.findIndex(o => o.key === v) === -1) {
        opts.push({key: v, text: v, value: v});
      }
    });
  }

  const handleAddition = (newValue) => {
    if (Array.isArray(value)) {
      onChange(value.concat(newValue));
    } else {
      onChange([newValue]);
    }
  }

  return (<Dropdown search selection fluid
    multiple
    allowAdditions={allowAdditions}
    options={opts}
    value={value}
    onAddItem={(_, data) => handleAddition(data.value)}
    onChange={(_, data) => onChange(data.value)} />);
};

export default MultiChoiceProp;
