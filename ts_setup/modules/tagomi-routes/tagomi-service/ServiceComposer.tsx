import React from 'react';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { LocaleLabels } from '../tagomi-locale';
import { useFetch } from '@dxs-ts/envir-fetch';


export const ServiceComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { backend, actions } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const [serviceName, setServiceName] = React.useState("");
  const [orchestratorName, setOrchestratorName] = React.useState("");
  const [labels, setLabels] = React.useState<TagomiApi.LocaleAndLabel[]>([]);
  const [changeInProgress, setChangeInProgress] = React.useState(false);

  const message = intl.formatMessage({ id: 'snack.service.createdMessage' }, { serviceName, orchestratorName });
  const { flows: allFlows = [] } = useFetch('worker/rest/api/assets/wrench/flow-names.GET', {});

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
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.service.create.dialog.title' })}</DialogTitle>

      <DialogContent>
        <Burger.TextField label='tagomi.service.create.dialog.serviceName' onChange={setServiceName} value={serviceName ? serviceName : ''} />
        <Burger.Select label="tagomi.service.create.dialog.orchestratorName" onChange={setOrchestratorName}
          selected={orchestratorName}
          items={allFlows.map((flow) => { return { id: flow, value: flow } })}
        />

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
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
