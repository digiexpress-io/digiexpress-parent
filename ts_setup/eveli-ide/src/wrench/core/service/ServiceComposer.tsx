import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@/burger';

import { useSnackbar } from 'notistack';

import { Composer, Client } from '../context';
import { ErrorView } from '../styles';


const ServiceComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const nav = Composer.useNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

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
      .catch((error: Client.StoreError) => {
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


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='services.composer.title'
    submit={{
      title: "buttons.create",
      disabled: apply,
      onClick: () => handleCreate()
    }}
  />);
}

export { ServiceComposer };


