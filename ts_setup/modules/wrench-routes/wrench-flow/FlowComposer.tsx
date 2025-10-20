import React from 'react';
import { Typography, Box, Dialog, DialogTitle, DialogContent, DialogActions, Button, FormHelperText } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';

import { useSnackbar } from 'notistack';


import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { ErrorView } from '../wrench-styles';
import { useWrenchNav } from '../wrench-nav';
import { CancelButton } from '@dxs-ts/eveli-primitives';


const FlowComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const intl = useIntl();
  const { service, actions } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().flow(name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="flows.composer.createdMessage" values={{ name }} />,
          { variant: 'success' }
        );
  
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.flows).filter(d => d.ast?.name === name);
          onNav({ type: 'ENTITY_EDITOR', id: article.id })
        });

        onClose();
      })
      .catch((error:HdesApi.StoreError) => {
        setErrors(error);
      });
  }

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="flows.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='flows.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCreate()} />
      {!name && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}

    </Typography>)
  }


  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='flows.composer.title' /></DialogTitle>
      <DialogContent>{editor}</DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={apply || !name}>
          <FormattedMessage id='buttons.create'/>
        </Button>
      </DialogActions>
    </Dialog>);
}

export { FlowComposer };


