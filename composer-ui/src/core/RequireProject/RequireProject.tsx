import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import { useSnackbar } from 'notistack';

import { Composer, Client } from '../context';
import Errors from '../Errors';


const RequireProject: React.FC<{ }> = ({ }) => {
  const { enqueueSnackbar } = useSnackbar();
  
  const { service, actions, site } = Composer.useComposer();
  const [open, setOpen] = React.useState(true);
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<Client.StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    service.create().site()
      .then(async data => {
        await actions.handleLoadSite(data);
        setApply(false);
        setOpen(false);
        enqueueSnackbar(<FormattedMessage id="project.dialog.requireProject.createdMessage" />);
      })
      .catch((error: Client.StoreError) => {
        setErrors(error);
        setApply(false);
      });
  }

  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="project.dialog.requireProject.errorsTitle" />
      </Typography>
      <Errors error={errors} />
    </Box>)
  } else {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="project.dialog.requireProject.content" values={{ name: site.name }}/>
      </Typography>
    </Box>)
  }

  return (<Burger.Dialog open={open} onClose={() => setOpen(false)}
    children={editor}
    backgroundColor="uiElements.main"
    title='project.dialog.requireProject.title'
    submit={{
      title: "buttons.create",
      disabled: apply,
      onClick: handleCreate
    }}
  />);
}

export { RequireProject };

