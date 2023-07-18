import React from 'react'

import moment from 'moment-timezone';
import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, Box, TextField, FormControl, InputLabel, Select, MenuItem, Grid } from '@material-ui/core';

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
      //marginLeft: theme.spacing(1),
      //marginRight: theme.spacing(1),
    },
  }),
);

const hitPolicyOptions = [
  { key: 'ALL', value: 'ALL', text: 'ALL' },
  { key: 'FIRST', value: 'FIRST', text: 'FIRST' }
];

interface MetaEditorProps {
  children: Hdes.AstAPI.Dt;
  model: Hdes.ModelAPI.Model;
  onClose: () => void;
  onChange: (commands: Hdes.AstAPI.DtCommand[]) => void;
}

const MetaEditor: React.FC<MetaEditorProps> = ({ children, onChange, model, onClose }) => {
  const classes = useStyles();

  const created = moment(model.created).format('DD.MM.YYYY HH:mm:ss');
  const modified = moment(model.modified).format('DD.MM.YYYY HH:mm:ss');

  const [name, setName] = React.useState(children.name);
  const [desc, setDesc] = React.useState(children.description);
  const [hitpolicy, setHitpolicy] = React.useState(children.hitPolicy);


  const editor = (
    <Box component="form" noValidate autoComplete="off">
      <TextField fullWidth sx={{ pb: 1 }}
        label={<FormattedMessage id='dt.menu.name' />}
        value={name}
        onChange={({ target }) => setName(target.value)} />

      <TextField fullWidth sx={{ pb: 1 }}
        label={<FormattedMessage id='dt.menu.description' />}
        value={desc ? desc : ""}
        onChange={({ target }) => setDesc(target.value)} />

      <FormControl variant="filled" fullWidth sx={{ pb: 1 }}>
        <InputLabel><FormattedMessage id='dt.menu.hitpolicy' /></InputLabel>
        <Select fullWidth
          onChange={({ target }) => setHitpolicy(target.value)}
          value={hitpolicy}
          label={<FormattedMessage id='dt.menu.hitpolicy' />}>
          {hitPolicyOptions.map(v => (<MenuItem key={v.value} value={v.value}>{v.text}</MenuItem>))}
        </Select>
      </FormControl>

      <Grid container spacing={2}>
        <Grid item xs={6}>
          <TextField fullWidth sx={{ pb: 1 }} disabled
            label={<FormattedMessage id='dt.menu.created' />}
            defaultValue={created} />
        </Grid>
        <Grid item xs={6}>
          <TextField fullWidth disabled
            label={<FormattedMessage id='dt.menu.modified' />}
            defaultValue={modified} />
        </Grid>
      </Grid>
    </Box>
  );

  const handleSave = () => {
    const commands: Hdes.AstAPI.DtCommand[] = [];
    if (name !== children.name) {
      commands.push({ type: "SET_NAME", value: name, id: "" });
    }
    if (hitpolicy !== children.hitPolicy) {
      commands.push({ type: "SET_HIT_POLICY", value: hitpolicy, id: "" });
    }
    if (desc !== children.description) {
      commands.push({ type: "SET_DESCRIPTION", value: desc, id: "" });
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
    title={<span><FormattedMessage id='dt.menu.header' values={{ name: model.name }} /></span>}
    open={true} />);
}

export type { MetaEditorProps };
export default MetaEditor;
