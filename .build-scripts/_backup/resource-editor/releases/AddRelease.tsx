import React from 'react';
import { FormattedMessage } from 'react-intl';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, TextField, Button, Box, CircularProgress } from '@material-ui/core';

import Resource from '../';
import { Hdes } from '../deps';
import { ErrorList } from '../errors';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
    },
    button: {
      marginTop: theme.spacing(1),
    }
  }),
);

interface AddReleaseProps {

};

const AddRelease: React.FC<AddReleaseProps> = () => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const [name, setName] = React.useState('');
  const [msg, setMsg] = React.useState('');
  const [saveErrors, setSaveErrors] = React.useState<Hdes.StoreError>();
  const [loading, setLoading] = React.useState(false);

  const tags = resource.session.models.TAG;

  const nameNotUnique = tags.filter(g => g.name === name).length > 0;
  const nameMustBeDefine = (name ? name.trim() : '').length === 0;

  let helperText: React.ReactElement;
  if (nameNotUnique) {
    helperText = <FormattedMessage id="releases.add.error.unique" />;
  } else if (nameMustBeDefine) {
    helperText = <FormattedMessage id="releases.add.error.filled" />;
  } else {
    helperText = (<></>);
  }
  const isError = nameNotUnique || nameMustBeDefine;

  const handleSave = () => {
    setLoading(true);
    resource.actions.createAsset({ name, serviceType: "TAG" })
      .then((asset) => {
        setName('');
        setMsg('')
        setLoading(false);
        setSaveErrors(undefined);
        return asset;
      })
      .catch((error) => {
        setSaveErrors(error);
        setLoading(false);
      });
  }

  return (<div className={classes.root}>
    <TextField autoFocus margin="dense" id="name" label={<FormattedMessage id="releases.add.input.name" />} type="text" fullWidth
      error={isError && name ? true : false} helperText={helperText}
      onChange={({ target }) => setName(target.value)}
      value={name} />
    <TextField autoFocus margin="dense" id="message" label={<FormattedMessage id="releases.add.input.msg" />} type="text" fullWidth
      onChange={({ target }) => setMsg(target.value)}
      value={msg} />
    <ErrorList>{saveErrors}</ErrorList>

    <Button variant="contained" color="primary" className={classes.button} disabled={isError || loading} onClick={handleSave}>
        {loading ? <Box sx={{pr: 1}}><CircularProgress size={15}/></Box> : null }
      <FormattedMessage id={"releases.add.button.create"} />
    </Button>
  </div>);
}

export type { AddReleaseProps };
export { AddRelease };
