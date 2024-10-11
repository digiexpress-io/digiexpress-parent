import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';

import { useSnackbar } from 'notistack';

import { Composer, Client } from '../context';
import { ErrorView } from '../styles';


const FlowComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { service, actions } = Composer.useComposer();
  const nav = Composer.useNav();

  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().flow(name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="flows.composer.createdMessage" values={{ name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.flows).filter(d => d.ast?.name === name);
          nav.handleInTab({ article })
        });

        onClose();
      })
      .catch((error: Client.StoreError) => {
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
    </Typography>)
  }


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='flows.composer.title'
    submit={{
      title: "buttons.create",
      disabled: apply,
      onClick: () => handleCreate()
    }}
  />);
}

export { FlowComposer };


