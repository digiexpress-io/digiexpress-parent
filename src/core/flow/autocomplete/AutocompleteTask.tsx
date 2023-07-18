import React from 'react'

import { Box, List, ListItem, ListItemText, Typography, Divider } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl'
import { useSnackbar } from 'notistack';
import Burger from '@the-wrench-io/react-burger';

import { Client, Composer } from '../../context';
import { FlowAstAutocomplete, toLowerCamelCase, executeTemplate } from './api';


const SelectTask: React.FC<{ value: Client.Entity<Client.AstBody>, onClick: () => void, linked: boolean }> = ({ value, onClick, linked }) => {
  const { ast } = value;
  if (!ast) {
    return null;
  }

  return (<>
    <ListItem alignItems="flex-start" sx={{ cursor: "pointer" }} onClick={onClick}>
      <ListItemText
        primary={`${linked ? '* ' : ''}${ast.name}`}
        secondary={<Typography
          sx={{ display: 'inline' }}
          component="span"
          variant="body2"
          color="text.primary">
          {ast.description}
        </Typography>} />
    </ListItem>
    <Divider />
  </>);

}

interface AutocompleteTaskProps {
  onClose: () => void;
  flow: Client.Entity<Client.AstFlow>;
  cm: CodeMirror.Editor;
  data: CodeMirror.Hints;
  cur: CodeMirror.Hint;
  guided: FlowAstAutocomplete
}

const AutocompleteTask: React.FC<AutocompleteTaskProps> = ({ onClose, cm, guided, flow }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { decisions, services } = Composer.useSite();
  const { actions, service } = Composer.useComposer();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [type, setType] = React.useState<Client.AstBodyType | string>(guided.guided === "decision-task" ? "DT" : "FLOW_TASK");
  const [link, setLink] = React.useState<Client.AstBody>();
  const usedLinks = flow.associations.filter(l => l.id && l.owner).map(l => l.id);
  const usedNames = [...Object.values(decisions).map(d => d.ast?.name), ...Object.values(services).map(d => d.ast?.name)]

  const assets: Client.Entity<Client.AstBody>[] = React.useMemo(() => {
    const target: Client.Entity<Client.AstBody>[] = type === "DT" ? Object.values(decisions) : Object.values(services);
    const keyword = name.toLowerCase();
    const result: Client.Entity<Client.AstBody>[] = target.filter(t => t.ast && (
      t.ast?.name.toLowerCase().indexOf(keyword) > -1 ||
      (t.ast?.description && t.ast?.description?.toLowerCase().indexOf(keyword) > -1)));
    return result;
  }, [name, type, services, decisions]);


  const handleSave = () => {
    setApply(true);
    
    const toBeReplaced = {
      name: name,
      id: toLowerCamelCase(name),
      collection: false,
      serviceType: type === "DT" ? "decisionTable" : "service",
      ref: name
    }

    if (link) {
      executeTemplate(cm, toBeReplaced, guided, link);
      onClose();
    } else if (type === "DT") {
      const serviceName = toBeReplaced.name;
      enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.creating" values={{name: serviceName, type}}/>);
      
      service.create().decision(serviceName).then(newSite => {
        enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.created" values={{name: serviceName, type}}/>);
                
        const newAsset = Object.values(newSite.services).filter(a => a.ast?.name === serviceName);
        if (newAsset.length === 1) {
          executeTemplate(cm, toBeReplaced, guided, newAsset[0].ast);
        }
        actions.handleLoadSite(newSite);
        onClose();
      });
    } else if (type === "FLOW_TASK") {
      const serviceName = toBeReplaced.name.charAt(0).toUpperCase() + toBeReplaced.name.slice(1);
      enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.creating" values={{name: serviceName, type}}/>);
      service.create().service(serviceName).then(newSite => {
        enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.created" values={{name: serviceName, type}}/>);
        
        const newAsset = Object.values(newSite.services).filter(a => a.ast?.name === serviceName);
        if (newAsset.length === 1) {
          executeTemplate(cm, toBeReplaced, guided, newAsset[0].ast);
        }
        actions.handleLoadSite(newSite);
        onClose();
      });
    }
  }

  return (<Burger.Dialog title="flows.autocomplete.task" backgroundColor="uiElements.main" open={true} onClose={onClose}
    actions={<Burger.SecondaryButton label="flows.autocomplete.task.create"
      disabled={usedNames.includes(name) || name.trim().length === 0 || apply || link ? true : false }
      onClick={() => handleSave()} />}
    submit={{
      title: "flows.autocomplete.task.link",
      disabled: (link ? false : true) || apply,
      onClick: () => handleSave()
    }}>
    <Box>
      <Burger.Select
        selected={type}
        onChange={(newType) => {
          setLink(undefined);
          setType(newType);
        }}
        label='flows.autocomplete.task.selectType'
        items={[
          { id: "DT", value: "Decision tables" },
          { id: "FLOW_TASK", value: "Flow tasks" }
        ]}
      />
      <Burger.TextField
        label='flows.autocomplete.task.searchField'
        placeholder={intl.formatMessage({id: 'flows.autocomplete.task.searchPlaceholder'})}
        helperText='flows.autocomplete.task.searchHelper'
        value={name} onChange={(newName) => {
          if (link) {
            setLink(undefined);
          }
          setName(newName);
        }} />

      <Box pt={2} pb={2}>
        <Typography variant="h4" fontWeight="bold"><FormattedMessage id={"flows.autocomplete.task.searchResults"} /></Typography>
      </Box>
      <List sx={{ width: '100%', height: 400, bgcolor: 'background.paper', overflow: "auto"}}>
        {assets.map(a => {
          const linked = usedLinks.includes(a.id);
          const comp = linked + '-' + a.ast?.name;
          return {entity: a, linked, comp};
        }).sort((a, b) => a.comp.localeCompare(b.comp) )
          .map(a => <SelectTask key={a.entity.id} value={a.entity} linked={a.linked} onClick={() => {
          setLink(a.entity.ast);
          setName((a.entity.ast as Client.AstBody).name);
        }} />)}
      </List>
    </Box>
  </Burger.Dialog >);
}

export { AutocompleteTask };
