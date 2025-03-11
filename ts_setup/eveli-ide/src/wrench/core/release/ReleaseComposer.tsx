import React from 'react';
import { Typography, Box, Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';

import { useSnackbar } from 'notistack';

import { Composer } from '../context';
import { ErrorView } from '../styles';
import { HdesApi } from '../client';
import { useWrenchNav } from '../nav';


const ReleaseComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [desc, setDesc] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().tag({name, desc})
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="releases.composer.createdMessage" values={{ name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.flows).filter(d => d.ast?.name === name);
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
        <FormattedMessage id="releases.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (
      <Typography variant="h4">
        <Burger.TextField
          label='releases.composer.assetName'
          value={name}
          onChange={setName}/>
        <Burger.TextField
          label='releases.composer.assetDesc'
          value={desc}
          onChange={setDesc}/>
      </Typography>)
  }


  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='releases.composer.title' /></DialogTitle>
    <DialogContent>{editor}</DialogContent>
    <DialogActions>
      <Button variant='text' onClick={onClose}>
        <FormattedMessage id='button.cancel'/>
      </Button>
      <Button onClick={handleCreate} disabled={apply}>
        <FormattedMessage id='buttons.create'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export { ReleaseComposer };
