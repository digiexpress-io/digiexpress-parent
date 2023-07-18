import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, TextField, FormControl, Select, InputLabel, Box, CircularProgress } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';
import Resource from '../';
import { ErrorList } from '../errors';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    button: {
      marginRight: theme.spacing(1),
    },
    text: {
      paddingTop: theme.spacing(2),
    }
  }),
);


type CopyAsDialogProps = {
  handleClose: () => void;
  onSave: (asset: Hdes.ResourceAPI.Asset) => void;
};

const CopyAsDialog: React.FC<CopyAsDialogProps> = ({ handleClose, onSave }) => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const [from, setFrom] = React.useState('');
  const [to, setTo] = React.useState('');
  const [saveErrors, setSaveErrors] = React.useState<Hdes.StoreError>();
  const [loading, setLoading] = React.useState(false);


  const heads = [
    ...resource.session.models.DT,
    ...resource.session.models.FLOW,
    ...resource.session.models.FLOW_TASK];

  const unique = heads.filter(asset => asset.name === to).length === 0;
  const empty = to.trim().length === 0;
  const error = !unique || empty;
  const errorText: React.ReactChild[] = [];
  if (!unique) {
    errorText.push(<FormattedMessage key={1} id={"dialog.copyas.error.unique"} />);
  }
  if (empty) {
    errorText.push(<FormattedMessage key={2} id={"dialog.copyas.error.filled"} />);
  }

  const content = (<>
    <div className={classes.text}>
      <FormControl variant="outlined" fullWidth>
        <InputLabel htmlFor="outlined-age-native-simple">{<FormattedMessage id={"dialog.copyas.input.from"} />}</InputLabel>
        <Select native
          value={from}
          onChange={({ target }) => setFrom(target.value as string)}
          label={<FormattedMessage id={"dialog.copyas.input.from"} />}
          inputProps={{
            name: 'from',
            id: 'outlined-from-native-simple',
          }}
        >
          <option value=""></option>
          {heads.map(head => (<option key={head.id} value={head.id}>{head.name}&nbsp;{head.type}</option>))}
        </Select>
      </FormControl>
    </div>
    <div className={classes.text}>
      <TextField required fullWidth
        error={error}
        id="filled-required"
        label={<FormattedMessage id={"dialog.copyas.input.to"} />}
        value={to}
        variant="outlined"
        helperText={errorText}
        onChange={({ target }) => setTo(target.value)}
      />
    </div>
    <ErrorList>{saveErrors}</ErrorList>
  </>);

  const handleSave = () => {
    setLoading(true);
    resource.actions.copyAsset({ from, to })
      .then((asset) => {
        onSave(asset);
        return asset;
      })
      .then(asset => {
        handleClose();
        return asset;
      })
      .catch((error) => {
        setSaveErrors(error);
        setLoading(false);
      });
  }


  const actions = (
    <>
      <Button color="secondary" className={classes.button} onClick={handleClose}><FormattedMessage id={"dialog.copyas.button.cancel"} /></Button>
      <Button variant="contained" color="primary" className={classes.button} onClick={handleSave} disabled={errorText.length > 0 || loading}>
        {loading ? <Box sx={{ pr: 1 }}><CircularProgress size={15} /></Box> : null}
        <FormattedMessage id={"dialog.copyas.button.confirm"} /></Button>
    </>);

  return (<Dialog
    title={<FormattedMessage id={"dialog.copyas.title"} />}
    open={true}
    onClose={handleClose}
    content={content}
    actions={actions}
  />);
}


export type { CopyAsDialogProps };
export { CopyAsDialog };


