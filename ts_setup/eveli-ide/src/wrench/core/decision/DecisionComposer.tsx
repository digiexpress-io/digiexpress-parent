import React from 'react';
import { Typography, Box,  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';

import { useSnackbar } from 'notistack';

import { Composer } from '../context';
import { HdesApi } from '../client';
import { ErrorView } from '../styles';



const DecisionComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const nav = Composer.useNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().decision(name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="decisions.composer.createdMessage" values={{ name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.decisions).filter(d => d.ast?.name === name);
          nav.handleInTab({ article })
        });

        onClose();
      })
      .catch((error: HdesApi.StoreError) => {
        setErrors(error);
      });
  }

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="decisions.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='decisions.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCreate()} />
    </Typography>)
  }
  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='decisions.composer.title' /></DialogTitle>
    <DialogContent>{editor}</DialogContent>
    <DialogActions>
      <Button variant='text' onClick={onClose}>
        <FormattedMessage id='button.cancel'/>
      </Button>
      <Button onClick={() => handleCreate()} disabled={apply}>
        <FormattedMessage id='buttons.create'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export { DecisionComposer };