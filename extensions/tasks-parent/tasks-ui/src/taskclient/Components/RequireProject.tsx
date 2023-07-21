import React from 'react';
import { Typography, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import { useSnackbar } from 'notistack';

import { useComposer } from '../hooks';
import { StoreError } from '../error-types';
import Errors from './ErrorView';


const RequireProject: React.FC<{ }> = ({ }) => {
  const { enqueueSnackbar } = useSnackbar();
  
  const { client, actions, site } = useComposer();
  const [open, setOpen] = React.useState(true);
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<StoreError>();

  const handleCreate = () => {
    setErrors(undefined);
    setApply(true);

    client.profile.createProfile()
      .then(async data => {
        await actions.handleLoadProfile(data);
        setApply(false);
        setOpen(false);
        enqueueSnackbar(<FormattedMessage id="project.dialog.requireProject.createdMessage" />);
      })
      .catch((error: StoreError) => {
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

export default RequireProject;

