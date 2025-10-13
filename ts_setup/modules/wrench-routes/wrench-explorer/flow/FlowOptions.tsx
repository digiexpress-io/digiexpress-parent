import React from 'react';
import { FormattedMessage } from 'react-intl';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';
import { ScienceOutlined as ScienceOutlinedIcon } from '@mui/icons-material';
import { Typography, Box, Dialog, DialogTitle, DialogContent, DialogActions, Button } from "@mui/material";

import { useSnackbar } from 'notistack';
import * as Burger from '@dxs-ts/eveli-primitives';



import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import {ErrorView} from '../../wrench-styles';
import { useWrenchNav, useWrenchDebug } from '../../wrench-nav';
import { CancelButton } from '@dxs-ts/eveli-primitives';


const FlowDelete: React.FC<{ flowId:HdesApi.FlowId, onClose: () => void }> = ({ flowId, onClose }) => {
  const { flows } = Composer.useSite();
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();
  const { findTab, onTabClose } = useWrenchNav();

  const flow = flows[flowId];
  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="flows.delete.error.title" />
      </Typography>
      <ErrorView error={errors}/>
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="flows.delete.content" values={{ name: flow.ast?.name }} />
    </Typography>)
  }

  const handleDelete = () => {
    setErrors(undefined);
    setApply(true);
    const flowTab = findTab('ENTITY_EDITOR', flowId);
    service.delete().flow(flowId)
      .then(async data => {
        await actions.handleLoadSite(data);
        if (flowTab) {
          onTabClose(flowTab);
        }
        enqueueSnackbar(<FormattedMessage id="flows.deleted.message" values={{ name: flow.ast?.name }} />,
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
    <DialogTitle><FormattedMessage id='flows.delete.title' /></DialogTitle>
    <DialogContent>{editor}</DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={handleDelete} disabled={apply}>
        <FormattedMessage id='buttons.delete'/>
      </Button>
    </DialogActions>
  </Dialog>);
}


const FlowOptions: React.FC<{ flow:HdesApi.Entity<HdesApi.AstFlow> }> = ({ flow }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'FlowDelete' | 'FlowCopy'>(undefined);
  const { onNav } = useWrenchNav();
  const {handleDebugInit} = useWrenchDebug();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(flow.ast?.name + "_copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    service.copy(flow.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="flows.composer.copiedMessage" values={{ name: flow.ast?.name, newName: name }} />,
          { variant: 'success' }
        );        
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.flows).filter(d => d.ast?.name === name);
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
        onEnter={() => handleCopy()} />
    </Typography>)
  }
  

  return (
    <>
      {dialogOpen === 'FlowDelete' ? <FlowDelete flowId={flow.id} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption nodeId={flow.id + 'edit-nested'}
        color='primary'
        icon={EditIcon}
        onClick={() => onNav({ type: 'ENTITY_EDITOR', id: flow.id })}
        labelText={<FormattedMessage id="flows.edit.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={flow.id + 'simulate-nested'}
        color='primary'
        icon={ScienceOutlinedIcon}
        onClick={() => handleDebugInit(flow.id)}
        labelText={<FormattedMessage id="flows.simulate.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={flow.id + 'delete-nested'}
        color='primary'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('FlowDelete')}
        labelText={<FormattedMessage id="flows.delete.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={flow.id + 'copyas-nested'}
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('FlowCopy')}
        labelText={<FormattedMessage id="flows.copyas.title" />}>
      </Burger.TreeItemOption>

      {dialogOpen === 'FlowCopy' ? (
        <Dialog open={true} onClose={handleDialogClose}>
          <DialogTitle><FormattedMessage id='flows.composer.copyTitle' /></DialogTitle>
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

export default FlowOptions;
