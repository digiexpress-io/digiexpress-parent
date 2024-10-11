import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Typography, Box } from "@mui/material";
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';

import { useSnackbar } from 'notistack';

import * as Burger from '@/burger';

import { Composer } from '../../context';
import { HdesApi } from '../../client';
import { ErrorView } from '../../styles';


const DecisionDelete: React.FC<{ decisionId: HdesApi.DecisionId, onClose: () => void }> = ({ decisionId, onClose }) => {
  const { decisions } = Composer.useSite();
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [apply, setApply] = React.useState(false);
  const [errors, setErrors] = React.useState<HdesApi.StoreError>();
  const tabs = Burger.useTabs();

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


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='decisions.delete.title'
    submit={{
      title: "buttons.delete",
      disabled: apply,
      onClick: () => {
        setErrors(undefined);
        setApply(true);
        var decisionTab = tabs.session.tabs.find(tab => tab.id === decisionId);
        service.delete().decision(decisionId)
          .then(data => {
            if (decisionTab) {
              tabs.actions.handleTabClose(decisionTab);
            }
            enqueueSnackbar(<FormattedMessage id="decisions.deleted.message" values={{ name: decision.ast?.name }} />);
            actions.handleLoadSite(data);
            onClose();
          })
          .catch((error: HdesApi.StoreError) => {
            setErrors(error);
          });
      }
    }}
  />);
}


const DecisionOptions: React.FC<{ decision: HdesApi.Entity<HdesApi.AstDecision> }> = ({ decision }) => {
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'DecisionDelete' | 'DecisionCopy'>(undefined);
  const nav = Composer.useNav();
  const {handleDebugInit} = Composer.useDebug();
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
        enqueueSnackbar(<FormattedMessage id="decisions.composer.copiedMessage" values={{ name: decision.ast?.name, newName: name }} />);
        actions.handleLoadSite(data).then(() => {
          const [article] = Object.values(data.decisions).filter(d => d.ast?.name === name);
          nav.handleInTab({ article })
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
        onClick={() => nav.handleInTab({ article: decision })}
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
      {dialogOpen === 'DecisionCopy' ? 
      <Burger.Dialog open={true}
        onClose={handleDialogClose}
        children={editor}
        backgroundColor="uiElements.main"
        title='decisions.composer.copyTitle'
        submit={{
          title: "buttons.copy",
          disabled: apply,
          onClick: () => handleCopy()
        }}
      /> : null}
    </>
  );
}

export default DecisionOptions;
