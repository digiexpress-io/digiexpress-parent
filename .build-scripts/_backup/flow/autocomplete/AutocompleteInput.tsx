import React from 'react'

import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, TextField, FormControl, InputLabel, Select, MenuItem } from '@material-ui/core';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';
import { HelperContext } from './';
import {parseTemplate} from './util';


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


interface AutocompleteInputProps {
  onClose: () => void;
  fl: Hdes.AstAPI.Fl;
  context: HelperContext;
}

const AutocompleteInput: React.FC<AutocompleteInputProps> = ({ context, fl, onClose }) => {
  const classes = useStyles();
  const [name, setName] = React.useState('');
  const [debug, setDebug] = React.useState('');
  const [dataType, setDataType] = React.useState('STRING');

  const types = fl.src.types
    .filter(h => !h.ref)
    .map(h => ({ key: h.value, value: h.value, text: h.value }))
  const refs = fl.src.types
    .filter(h => h.ref)
    .map(h => ({ key: h.ref, value: h.ref, text: h.ref }))
  refs.push({ key: '', value: '', text: '' })


  const handleSave = () => {
    const value = context.src.value;
    const editor = context.cm;
    const doc = editor.getDoc()
    const cursor = doc.getCursor()
    const content = editor.getLine(cursor.line)
    const toBeReplaced = {
      required: true,
      type: dataType,
      name: name ? name : 'myParam',
      debugValue: debug ? debug : null
    }

    const autocompleteLines = parseTemplate(toBeReplaced, value);
    doc.replaceRange(autocompleteLines,
      { line: cursor.line, ch: 0 },
      { line: cursor.line, ch: content.length }, 
      '+input');
    onClose();
  }

  const editor = (<>
    <TextField fullWidth sx={{ pb: 1 }}
      label={<FormattedMessage id='fl.autocomplete.input.name' />}
      value={name}
      onChange={({ target }) => setName(target.value)} />
    <TextField fullWidth sx={{ pb: 1 }}
      label={<FormattedMessage id='fl.autocomplete.input.debug' />}
      value={debug}
      onChange={({ target }) => setDebug(target.value)} />
    <FormControl variant="filled" fullWidth sx={{ pb: 1 }}>
      <InputLabel><FormattedMessage id='fl.autocomplete.input.dataType' /></InputLabel>
      <Select fullWidth
        onChange={({ target }) => setDataType(target.value)}
        value={dataType}
        label={<FormattedMessage id='fl.autocomplete.input.dataType' />}>
        {types.map(v => (<MenuItem key={v.value} value={v.value}>{v.text}</MenuItem>))}
      </Select>
    </FormControl>
  </>);

  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);

  return (<Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={onClose}
    actions={actions}
    title={<span><FormattedMessage id='fl.autocomplete.input' /></span>}
    open={true} />);
}

export { AutocompleteInput };


