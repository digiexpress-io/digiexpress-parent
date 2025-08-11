import React from 'react'
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'

import * as Burger from '@dxs-ts/eveli-primitives';
import { WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { FormattedMessage } from 'react-intl';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const MigrationComposer: React.FC<{ onClose: () => void}> = ({onClose}) => {
  const [file, setFile] = React.useState<string | undefined>();
  const [loading, setLoading] = React.useState<boolean | undefined>();
  const { service, actions } = Composer.useComposer();
  const handleCreate = () => {
    if (!file) {
      return;
    }
    setLoading(true);
    service.create().importData(file)
      .then(() => actions.handleLoadSite())
      .then(() => {
        setLoading(false);
        setFile(undefined);
        onClose();
      });
  }

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='migrations.title' /></DialogTitle>
    <DialogContent><Burger.FileField value="" onChange={setFile} label="migrations.select" /></DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={handleCreate} disabled={loading || !file}>
        <FormattedMessage id='migrations.create'/>
      </Button>
    </DialogActions>
  </Dialog>
  );
}

export { MigrationComposer };
