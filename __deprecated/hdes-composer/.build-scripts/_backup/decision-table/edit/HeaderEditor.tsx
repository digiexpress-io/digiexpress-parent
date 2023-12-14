import React from 'react'

import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, Box, TextField, FormControl, InputLabel, Select, MenuItem } from '@material-ui/core';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {

    },
    button: {
      marginRight: theme.spacing(1),
    },
    dialog: {
      marginTop: theme.spacing(1),
    },
  }),
);


interface HeaderEditorProps {
  dt: Hdes.AstAPI.Dt;
  header: Hdes.AstAPI.DtHeader;
  onClose: () => void;
  onChange: (commands: Hdes.AstAPI.DtCommand[]) => void;
}

const HeaderEditor: React.FC<HeaderEditorProps> = ({ dt, onChange, header, onClose }) => {
  const classes = useStyles();

  const [name, setName] = React.useState(header.name);
  const [dataType, setDataType] = React.useState(header.value);
  const [order, setOrder] = React.useState<string>(header.id);
  const [direction, setDirection] = React.useState(header.direction);
  const [script, setScript] = React.useState(header.script);

  const editor = (
    <Box component="form" noValidate autoComplete="off">
      <TextField fullWidth sx={{ pb: 1 }}
        label={<FormattedMessage id='dt.header.name' />}
        value={name}
        onChange={({ target }) => setName(target.value)} />

      <FormControl variant="filled" fullWidth sx={{ pb: 1 }}>
        <InputLabel><FormattedMessage id='dt.header.dataType' /></InputLabel>
        <Select fullWidth
          onChange={({ target }) => setDataType(target.value)}
          value={dataType}
          label={<FormattedMessage id='dt.header.dataType' />}>
          {dt.headerTypes.map(v => (<MenuItem key={v} value={v}>{v}</MenuItem>))}
        </Select>
      </FormControl>
      <FormControl variant="filled" fullWidth sx={{ pb: 1 }}>
        <InputLabel><FormattedMessage id='dt.header.direction' /></InputLabel>
        <Select fullWidth
          onChange={({ target }) => setDirection(target.value as any)}
          value={direction}
          label={<FormattedMessage id='dt.header.direction' />}>
          {["IN", "OUT"].map(v => (<MenuItem key={v} value={v}>{v}</MenuItem>))}
        </Select>
      </FormControl>

      { direction === header.direction ?
        (<FormControl variant="filled" fullWidth sx={{ pb: 1 }}>
          <InputLabel><FormattedMessage id='dt.header.order' /></InputLabel>
          <Select fullWidth
            onChange={({ target }) => setOrder(target.value)}
            value={order}
            label={<FormattedMessage id='dt.header.order' />}>
            {dt.headers.filter(v => v.direction === direction).map(v => (<MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>))}
          </Select>
        </FormControl>) : null
      }

      { direction === "OUT" ?
        (<TextField fullWidth sx={{ pb: 1 }}
          label={<FormattedMessage id='dt.header.script' />}
          value={script}
          onChange={({ target }) => setScript(target.value)} />
        ) : null
      }

    </Box>
  );

  const handleSave = () => {

    const commands: Hdes.AstAPI.DtCommand[] = [];
    if (name !== header.name) {
      commands.push({ type: "SET_HEADER_REF", value: name, id: header.id });
    }
    if (dataType !== header.value) {
      commands.push({ type: "SET_HEADER_TYPE", value: dataType, id: header.id });
    }
    if (direction !== header.direction) {
      commands.push({ type: "SET_HEADER_DIRECTION", value: direction, id: header.id });
    }
    if (script !== header.script && direction === "OUT") {
      commands.push({ type: "SET_HEADER_SCRIPT", value: script, id: header.id });
    }
    if (order !== header.id && direction === header.direction) {
      commands.push({ type: "MOVE_HEADER", value: order, id: header.id });
    }
    
    if(commands.length > 0) {
      onChange(commands);
    }
    onClose();
  }

  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);

  return (<Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={onClose}
    actions={actions}
    title={<span><FormattedMessage id='dt.header.edit.title' values={{ name: header.name }} /></span>}
    open={true} />);
}

export type { HeaderEditorProps };
export default HeaderEditor;
