import React from 'react'

import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, TextField } from '@material-ui/core';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';
import { HelperContext } from './';
import { parseTemplate, toLowerCamelCase } from './util';


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


interface AutocompleteSwitchProps {
  onClose: () => void;
  fl: Hdes.AstAPI.Fl;
  context: HelperContext;
}

const AutocompleteSwitch: React.FC<AutocompleteSwitchProps> = ({ context, fl, onClose }) => {
  
  const classes = useStyles();
  const [name, setName] = React.useState('');
  
  const handleSave = () => {
    const value = context.src.value;
    const editor = context.cm
    const doc = editor.getDoc()
    const cursor = doc.getCursor()
    const content = editor.getLine(cursor.line)

    const toBeReplaced = {
        name: name,
        id: toLowerCamelCase(name),
        caseName: 'first case, points to next task in order',
        when: '"true"',
        then: 'next',
    }
    const lines = parseTemplate(toBeReplaced, value); 
    doc.replaceRange([...lines, ''],
        {line: cursor.line, ch: 0}, 
        {line: cursor.line, ch: content.length}, '+input')
    onClose();    
  }
  const editor = (<TextField fullWidth sx={{ pb: 1 }}
      label={<FormattedMessage id='fl.autocomplete.switch.name' />}
      value={name}
      onChange={({ target }) => setName(target.value)} />);
      

  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);  

  return (<Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={onClose}
    actions={actions}
    title={<span><FormattedMessage id='fl.autocomplete.switch' /></span>}
    open={true} />);
}

export { AutocompleteSwitch };

