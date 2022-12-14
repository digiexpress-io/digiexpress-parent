import React from 'react';

import { CircularProgress, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import DeClient from '@declient';


const Migration: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const [open, setOpen] = React.useState(true);
  const [file, setFile] = React.useState<string | undefined>();
  const [loading, setLoading] = React.useState<boolean | undefined>();
  const [errors, setErrors] = React.useState<DeClient.StoreError>();
  const { client, actions } = DeClient.useComposer();

  const handleCreate = () => {
    if (!file) {
      return;
    }
    setLoading(true);
    client.create().migrate(JSON.parse(file))
      .then(() => actions.handleLoadHead())
      .then(() => {
        setLoading(false);
        setFile(undefined);
        setOpen(false);
        onClose();
      })
      .catch((error: DeClient.StoreError) => {
        setErrors(error);
        setLoading(false);
      });;
  }

  return (
    <Burger.Dialog open={open} onClose={onClose}
      backgroundColor="uiElements.main"
      title="migrations.dialog.title"
      submit={{ title: "buttons.create", onClick: handleCreate, disabled: loading || !file }}>
      <>
        {errors ? (<Box>
          <Typography variant="h4">
            <FormattedMessage id="project.dialog.requireProject.errorsTitle" />
          </Typography>
          <DeClient.Error error={errors} />
        </Box>) : undefined}
        {loading ? <CircularProgress /> : <Burger.FileField value="" onChange={setFile} label="migrations.dialog.select" />}
      </>
    </Burger.Dialog>


  );
}

export { Migration };
