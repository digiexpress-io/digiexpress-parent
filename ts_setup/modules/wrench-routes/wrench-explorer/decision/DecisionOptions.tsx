import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Typography, Box, Dialog, DialogTitle, DialogContent, DialogActions, Button } from "@mui/material";

import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ScienceOutlined as ScienceOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';

import { useSnackbar } from 'notistack';

import * as Burger from '@dxs-ts/eveli-primitives';


import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { ErrorView } from '../../wrench-styles';
import { useWrenchNav, useWrenchDebug } from '../../wrench-nav';
import { CancelButton } from '@dxs-ts/eveli-primitives';


const DecisionDelete: React.FC<{ decisionId: HdesApi.DecisionId, onClose: () => void }> = ({ decisionId, onClose }) => {
  const { decisions } = Composer.useSite();
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();
  const { onTabClose, findTab } = useWrenchNav();

  const decision = decisions[decisionId];
  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="decisions.delete.error.title" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <FormattedMessage id="decisions.delete.content" values={{ name: decision.ast?.name }} />
    </Typography>)
  }


  const handleSubmit = () => {
    setErrors(undefined);
    setApply(true);
    const decisionTab = findTab('ENTITY_EDITOR', decisionId);
    service.delete().decision(decisionId)
      .then(async data => {

        enqueueSnackbar(<FormattedMessage id="decisions.deleted.message" values={{ name: decision.ast?.name }} />,
          { variant: 'success' }
        );
        
        await actions.handleLoadSite(data);

        if (decisionTab) {
          onTabClose(decisionTab);
        }
        onClose();
      })
      .catch((error: HdesApi.StoreError) => {
        setErrors(error);
      });
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.delete.title' /></DialogTitle>
      <DialogContent>{editor}</DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleSubmit} disabled={apply}>
          <FormattedMessage id='buttons.delete' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}


const DecisionOptions: React.FC<{ decision: HdesApi.Entity<HdesApi.AstDecision> }> = ({ decision }) => {
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'DecisionDelete' | 'DecisionCopy'>(undefined);
  const { onNav } = useWrenchNav()
  const { handleDebugInit } = useWrenchDebug();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState(decision.ast?.name + "_copy");
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();

  const handleCopy = () => {
    setErrors(undefined);
    setApply(true);

    service.copy(decision.id, name)
      .then(data => {
        enqueueSnackbar(<FormattedMessage id="decisions.composer.copiedMessage" values={{ name: decision.ast?.name, newName: name }} />,
          { variant: 'success' }
        );
        
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.decisions).filter(d => d.ast?.name === name);
          onNav({ type: 'ENTITY_EDITOR', id: article.id })
        });
        handleDialogClose();
      }).catch((error: HdesApi.StoreError) => {
        setErrors(error);
      });
  }


  let editor = (<></>);
  if (errors) {
    editor = (<Box>
      <Typography variant="h4">
        <FormattedMessage id="decisions.composer.errorsTitle" />
      </Typography>
      <ErrorView error={errors} />
    </Box>)
  } else {
    editor = (<Typography variant="h4">
      <Burger.TextField
        label='decisions.composer.assetName'
        value={name}
        onChange={setName}
        onEnter={() => handleCopy()} />
    </Typography>)
  }



  return (
    <>
      {dialogOpen === 'DecisionDelete' ? <DecisionDelete decisionId={decision.id} onClose={handleDialogClose} /> : null}
      <Burger.TreeItemOption nodeId={decision.id + 'edit-nested'}
        color='page'
        icon={EditIcon}
        onClick={() => onNav({ type: 'ENTITY_EDITOR', id: decision.id })}
        labelText={<FormattedMessage id="decisions.edit.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={decision.id + 'simulate-nested'}
        color='page'
        icon={ScienceOutlinedIcon}
        onClick={() => handleDebugInit(decision.id)}
        labelText={<FormattedMessage id="decisions.simulate.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={decision.id + 'delete-nested'}
        color='page'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('DecisionDelete')}
        labelText={<FormattedMessage id="decisions.delete.title" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={decision.id + 'copyas-nested'}
        color='page'
        icon={EditIcon}
        onClick={() => setDialogOpen('DecisionCopy')}
        labelText={<FormattedMessage id="decisions.copyas.title" />}>
      </Burger.TreeItemOption>

      {dialogOpen === 'DecisionCopy' ? (
        <Dialog open={true} onClose={handleDialogClose}>
          <DialogTitle><FormattedMessage id='decisions.composer.copyTitle' /></DialogTitle>
          <DialogContent>{editor}</DialogContent>
          <DialogActions>
            <CancelButton onClick={handleDialogClose} />
            <Button onClick={() => handleCopy()} disabled={apply}>
              <FormattedMessage id='buttons.copy' />
            </Button>
          </DialogActions>
        </Dialog>
      ) : null}
    </>
  );
}

export default DecisionOptions;
