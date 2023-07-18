import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, FormControl, InputLabel, Select, TextField, CircularProgress, Box } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';


import { Hdes } from '../deps';
import Resource from '../';
import { Dialog } from '../dialog';
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

type AddTypeDialogProps = {
  handleClose: () => void;
  onSave: (asset: Hdes.ResourceAPI.Asset) => void;
};

const AddTypeDialog: React.FC<AddTypeDialogProps> = ({ handleClose, onSave }) => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const [serviceType, setServiceType] = React.useState<Hdes.ModelAPI.ServiceType>("DT");
  const [name, setName] = React.useState('');
  const [saveErrors, setSaveErrors] = React.useState<Hdes.StoreError>();
  const [loading, setLoading] = React.useState(false);


  const types: Hdes.ModelAPI.ServiceType[] = ["DT", "FLOW_TASK", "FLOW"];
  const heads = [
    ...resource.session.models.DT, 
    ...resource.session.models.FLOW, 
    ...resource.session.models.FLOW_TASK];
  
  const unique = heads.filter(asset => asset.name === name).length === 0;
  const empty = name.trim().length === 0;
  const error = !unique || empty;
  const errorText: React.ReactChild[] = [];
  
  if (!unique) {
    errorText.push(<FormattedMessage key={1} id={"dialog.addtype.error.unique"}/>);
  }
  if (empty) {
    errorText.push(<FormattedMessage key={2} id={"dialog.addtype.error.filled"}/>);
  }

  const content = (<>
    <div className={classes.text}>
      <FormControl variant="outlined" fullWidth>
        <InputLabel htmlFor="outlined-age-native-simple">{<FormattedMessage id={"dialog.addtype.input.serviceType"}/>}</InputLabel>
        <Select native
          value={serviceType}
          onChange={({ target }) => setServiceType(target.value as any)}
          label={<FormattedMessage id={"dialog.addtype.input.serviceType"}/>}
          inputProps={{
            name: 'from',
            id: 'outlined-from-native-simple',
          }}
        >
          {types.map(head => (<option key={head} value={head}>{head}</option>))}
        </Select>
      </FormControl>
    </div>
    <div className={classes.text}>
      <TextField required fullWidth
        error={error}
        id="filled-required"
        label={<FormattedMessage id={"dialog.addtype.input.name"}/>}
        value={name}
        variant="outlined"
        helperText={errorText}
        onChange={({ target }) => setName(target.value)}
      />
    </div>
    <ErrorList>{saveErrors}</ErrorList>
  </>
  );

  const handleSave = () => {
    setLoading(true);    
    resource.actions.createAsset({name, serviceType})
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
      <Button color="secondary" className={classes.button} onClick={handleClose}><FormattedMessage id={"dialog.addtype.button.cancel"}/></Button>
      <Button variant="contained" color="primary" className={classes.button} onClick={handleSave} disabled={errorText.length > 0 || loading}>
        {loading ? <Box sx={{pr: 1}}><CircularProgress size={15}/></Box> : null }
        <FormattedMessage id={"dialog.addtype.button.confirm"}/>
      </Button>
    </>);

  return (<Dialog
    title={<FormattedMessage id={"dialog.addtype.title"}/>}
    open={true}
    onClose={handleClose}
    content={content}
    actions={actions}
  />);
}


export type { AddTypeDialogProps };
export { AddTypeDialog };


