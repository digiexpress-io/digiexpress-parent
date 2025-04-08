import React from 'react';
import { Typography, Box,  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/eveli-styles';

import { useSnackbar } from 'notistack';

import { WrenchComposerApi as Composer } from '../wrench-setup';
import { HdesApi } from '@/api-wrench';
import { ErrorView } from '../wrench-styles';
import { useWrenchNav } from '../wrench-nav';



const DecisionComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().decision(name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="decisions.composer.createdMessage" values={{ name }} />,
          { variant: 'success' }
        );

        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.decisions).filter(d => d.ast?.name === name);
          onNav({ type: 'ENTITY_EDITOR', id: article.id })
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