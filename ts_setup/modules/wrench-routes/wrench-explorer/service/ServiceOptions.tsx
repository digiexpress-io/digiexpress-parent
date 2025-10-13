import React from 'react';
import { Typography, Box, Dialog, DialogTitle, DialogContent, DialogActions, Button, useTheme } from "@mui/material";
import { FormattedMessage } from 'react-intl';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ScienceOutlined as ScienceOutlinedIcon } from '@mui/icons-material';

import { useSnackbar } from 'notistack';
import * as Burger from '@dxs-ts/eveli-primitives';


import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { ErrorView } from '../../wrench-styles';
import { useWrenchNav, useWrenchDebug } from '../../wrench-nav';
import { CancelButton } from '@dxs-ts/eveli-primitives';


const ServiceDelete: React.FC<{ serviceId:HdesApi.ServiceId, onClose: () => void }> = ({ serviceId, onClose }) => {
  const { services } = Composer.useSite();
  const { service: composerService, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();
  const { findTab, onTabClose } = useWrenchNav();

  const service = services[serviceId];
  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="services.delete.error.title" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="services.delete.content" values={{ name: service.ast?.name }} />
    </Typography>)
  }

  const handleDelete = () => {
    setErrors(undefined);
    setApply(true);
    const serviceTab = findTab('ENTITY_EDITOR', serviceId);
    composerService.delete().service(serviceId)
      .then(async data => {
        await actions.handleLoadSite(data);
        if (serviceTab) {
          onTabClose(serviceTab);
        }
        enqueueSnackbar(<FormattedMessage id="services.deleted.message" values={{ name: service.ast?.name }} />,
          { variant: 'success' }
        );        
        onClose();
      })
      .catch((error:HdesApi.StoreError) => {
        setErrors(error);
      });
  }

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='services.delete.title' /></DialogTitle>
    <DialogContent>{editor}</DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={handleDelete} disabled={apply}>
        <FormattedMessage id='services.delete.title'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

const ServiceOptions: React.FC<{ service:HdesApi.Entity<HdesApi.AstService> }> = ({ service }) => {

  const theme = useTheme();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ServiceDelete' | 'ServiceCopy'>(undefined);
  const { onNav } = useWrenchNav();
  const {handleDebugInit} = useWrenchDebug();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service: clientService, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(service.ast?.name + "_Copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    clientService.copy(service.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="services.composer.copiedMessage" values={{ name: service.ast?.name, newName: name }} />,
          { variant: 'success' }
        );        
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.services).filter(d => d.ast?.name === name);
          onNav({ type: 'ENTITY_EDITOR', id: article.id })
        });
        handleDialogClose();
      }).catch((error:HdesApi.StoreError) => {
        setErrors(error);
      });
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
        onChange={setName}
        onEnter={() => handleCopy()} />
    </Typography>)
  }

  return (
    <>
      {dialogOpen === 'ServiceDelete' ? <ServiceDelete serviceId={service.id} onClose={handleDialogClose} /> : null}
      <Burger.TreeItemOption nodeId={service.id + 'edit-nested'}
        color={theme.palette.primary.light}
        onClick={() => onNav({ type: 'ENTITY_EDITOR', id: service.id })}
        labelText={<FormattedMessage id="services.edit.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'simulate-nested'}
        color={theme.palette.primary.light}
        icon={ScienceOutlinedIcon}
        onClick={() => handleDebugInit(service.id)}
        labelText={<FormattedMessage id="services.simulate.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'delete-nested'}
        color={theme.palette.primary.light}
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ServiceDelete')}
        labelText={<FormattedMessage id="services.delete.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'copyas-nested'}
        color={theme.palette.primary.light}
        onClick={() => setDialogOpen('ServiceCopy')}
        labelText={<FormattedMessage id="services.copyas.title" />}>
      </Burger.TreeItemOption>
      {dialogOpen === 'ServiceCopy' ? (
        <Dialog open={true} onClose={handleDialogClose}>
          <DialogTitle><FormattedMessage id='services.composer.copyTitle' /></DialogTitle>
          <DialogContent>{editor}</DialogContent>
          <DialogActions>
            <CancelButton onClick={handleDialogClose} />
            <Button onClick={handleCopy} disabled={apply}>
              <FormattedMessage id='buttons.copy'/>
            </Button>
          </DialogActions>
        </Dialog>) : null}
    </>
  );
}

export default ServiceOptions;
