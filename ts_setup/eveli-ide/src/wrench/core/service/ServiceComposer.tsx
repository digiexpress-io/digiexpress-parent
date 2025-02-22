import React from 'react';
import { Typography, Box,  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';

import { useSnackbar } from 'notistack';

import { Composer } from '../context';
import { ErrorView } from '../styles';
import { HdesApi} from '../client';

const ServiceComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const nav = Composer.useNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().service(name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="services.composer.createdMessage" values={{ name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.services).filter(d => d.ast?.name === name);
          nav.handleInTab({ article })
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
        <Button variant='text' onClick={onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={handleCreate} disabled={apply}>
          <FormattedMessage id='buttons.create'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { ServiceComposer };


