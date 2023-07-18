import React from 'react'


import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, TextField, Autocomplete, Stack } from '@material-ui/core';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';
import { HelperContext } from './';

import { parseTemplate, toLowerCamelCase } from './util';

const TASK_TYPES = {DT: 'decisionTable', FLOW_TASK: 'service'};

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


interface AutocompleteTaskProps {
  onClose: () => void;
  onCreate: (props: {name: string, type: Hdes.ModelAPI.ServiceType}) => Promise<void>;
  fl: Hdes.AstAPI.Fl;
  models: Hdes.ModelAPI.Models;
  context: HelperContext;
  type: "DT" | "FLOW_TASK"
}

const AutocompleteTask: React.FC<AutocompleteTaskProps> = ({ context, onClose, models, type, onCreate }) => {  
  const classes = useStyles();
  const [name, setName] = React.useState("");
  const options = (type === "DT" ? models.DT : models.FLOW_TASK);

  const handleSave = () => {
    const editor = context.cm
    const doc = editor.getDoc()
    const cursor = doc.getCursor()    
    const content = editor.getLine(cursor.line)

    const toBeReplaced = {
      name: name,
      id: toLowerCamelCase(name),
      collection: false,
      then: 'next',
      type: TASK_TYPES[type],
      ref: name
    }

    const executeTemplate = (value: any, template: string[], asset?: Hdes.ModelAPI.Model) => {
      const lines: string[] = parseTemplate(value, template)
      if(asset) {
        const params = asset.params
          .filter(p => p.direction === 'IN')
          .map(p => '          ' + p.name + ':');
        
        lines.push(...params)
      }
      
      doc.replaceRange([...lines, ''],
          {line: cursor.line, ch: 0}, 
          {line: cursor.line, ch: content.length}, '+input')
    }
    
    const serviceName = type === 'FLOW_TASK' ? toBeReplaced.name.charAt(0).toUpperCase() + toBeReplaced.name.slice(1) : toBeReplaced.name
    let asset = [...options.filter(a => a.name === name)].pop();

    if(asset) {
      toBeReplaced.ref = asset.name
      executeTemplate(toBeReplaced, context.src.value, asset);
      onClose();
    } else {
      onCreate({ name: serviceName, type: type }).then(() => {
        executeTemplate(toBeReplaced, context.src.value, asset);
        onClose();
      });
    }
  }

  const editor = (<Stack spacing={2} sx={{ width: 300 }}>
      <Autocomplete freeSolo disableClearable
        options={options.map((option) => option.name)}
        renderInput={(params) => (
          
          <TextField {...params} InputProps={{ ...params.InputProps, type: 'search' }}
            onChange={({target}) => setName(target.value)}
            label={<FormattedMessage id='fl.autocomplete.task.search.or.create' />}
          />
        )}
      />
    </Stack>
  );
  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);  

  return (<Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={onClose}
    actions={actions}
    title={<span><FormattedMessage id='fl.autocomplete.task' /></span>}
    open={true} />);
}

export { AutocompleteTask };
