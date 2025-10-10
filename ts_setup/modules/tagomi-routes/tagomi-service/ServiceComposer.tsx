import React from 'react';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';

import * as Burger from '@dxs-ts/eveli-primitives';

import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';

import { FormattedMessage } from 'react-intl';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { LocaleLabels } from '../tagomi-locale';


export const ServiceComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  const { backend, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [serviceName, setServiceName] = React.useState("");
  const [orchestratorName, setOrchestratorName] = React.useState("");
  const [labels, setLabels] = React.useState<TagomiApi.LocaleAndLabel[]>([]);
  const [changeInProgress, setChangeInProgress] = React.useState(false);

  const message = <FormattedMessage id="snack.tagomi.services.createdMessage" values={{ serviceName, orchestratorName }} />

  const handleCreate = () => {
    const entity: TagomiApi.CreateService = { serviceName, labels, orchestratorName };

    backend.createService(entity).then(success => {
      console.log(success)
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }


  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='tagomi.service.composer.title' /></DialogTitle>
      <DialogContent>

        <Burger.TextField label='tagomi.service.composer.serviceName' onChange={setServiceName} value={serviceName} />
        <Burger.TextField label='tagomi.service.composer.orchestratorName' onChange={setOrchestratorName} value={orchestratorName} />
        <Box mb={2} />

        <LocaleLabels
          onChange={(labels) => {
            setChangeInProgress(false);
            setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value })));
          }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!serviceName || changeInProgress}>
          <FormattedMessage id='tagomi.service.composer.create' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}
