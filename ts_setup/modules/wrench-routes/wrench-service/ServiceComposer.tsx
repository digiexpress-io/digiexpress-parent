import React from 'react';
import { Typography, Box,  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useSnackbar } from 'notistack';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

import { ErrorView } from '../wrench-styles';

import { useWrenchNav } from '../wrench-nav';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import * as Burger from '@dxs-ts/eveli-primitives';



const ServiceComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().service(name)
      .then(data => {
        enqueueSnackbar(
          <FormattedMessage id="services.composer.createdMessage" values={{ name }} />,
          { variant: 'success' }
        );        
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.services).filter(d => d.ast?.name === name);
          onNav({ type: 'ENTITY_EDITOR', id: article.id })
        });

        onClose();
      })
      .catch((error: HdesApi.StoreError) => {
        setErrors(error);
      });
  }

  const handleName = (toBeReplaced: string) => {
    const serviceName = toBeReplaced.charAt(0).toUpperCase() + toBeReplaced.slice(1);
    setName(serviceName);
  }

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="services.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='services.composer.assetName'
        value={name}
        onChange={handleName}
        onEnter={() => handleCreate()} />
    </Typography>)
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='services.composer.title' /></DialogTitle>
      <DialogContent>{editor}</DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={apply}>
          <FormattedMessage id='buttons.create'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { ServiceComposer };


