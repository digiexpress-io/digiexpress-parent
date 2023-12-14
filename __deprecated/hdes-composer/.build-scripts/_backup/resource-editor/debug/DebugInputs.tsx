import React from 'react';
import { Accordion, AccordionSummary, AccordionDetails, Typography, TextField, FormControl, InputLabel, Select, MenuItem } from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage } from 'react-intl';

import { Hdes } from '../deps';
import { useContext } from './context';


function Grid<T = any>(props: { elements: T[], render: (element: T, index: number, style: Object) => React.ReactElement }): React.ReactElement[] {
  const rows: React.ReactElement[] = [];
  let index = 1;
  let row: React.ReactElement[] = [];

  for (const input of props.elements) {
    const style = { width: "33.33%" };
    const field = props.render(input, index, style);
    row.push(field);

    if (row.length === 3 || index === props.elements.length) {
      rows.push((<div key={index}>{row}</div>));
      row = [];
    }
    index++;
  }
  return rows;
}

const DebugInputs: React.FC<{}> = () => {
  const context = useContext();
  const data = context.active;

  if (!data) {
    return null;
  }
  const model = data.model;
  const inputs = model.params.filter(p => p.direction === "IN");
  const getValue = (parameter: Hdes.ModelAPI.DataType) => {
    const init = data.debug.entity[parameter.name];
    if (init === undefined) {
      return parameter.values ? parameter.values : "";
    }
    return init;
  }


  return (
    <Accordion expanded={context.session.inputs} onChange={() => context.actions.handleInputs(!context.session.inputs)}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography color="primary"><FormattedMessage id={"debug.asset.execute.inputs"} /></Typography>
      </AccordionSummary>
      <AccordionDetails>
        {
          Grid({
            elements: inputs,
            render: (p, index, style) => p.valueType === 'BOOLEAN' ?
              (<FormControl variant="outlined" key={index} style={Object.assign(style, {marginTop: "8px", paddingLeft: "8px" })} required={p.required} >
                <InputLabel>{p.name}</InputLabel>
                <Select
                  value={getValue(p)}
                  onChange={({ target }) => context.actions.handleSetModelEntity({ modelId: data.model.id, entity: p.name, value: target.value })}
                >
                  <MenuItem value="">
                    <em>None</em>
                  </MenuItem>
                  <MenuItem value={"true"}>true</MenuItem>
                  <MenuItem value={"false"}>false</MenuItem>
                </Select>
              </FormControl>)
              :
              (<TextField key={index} style={style}
                onChange={({ target }) => context.actions.handleSetModelEntity({ modelId: data.model.id, entity: p.name, value: target.value })}
                required={p.required} label={p.name}
                value={getValue(p)} />
              )

          })
        }
      </AccordionDetails>
    </Accordion>
  );
}

export { DebugInputs };


