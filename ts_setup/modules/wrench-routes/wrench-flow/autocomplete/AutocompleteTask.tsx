import React from 'react'

import { Box, List, ListItem, ListItemText, Typography, Divider, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl'
import { useSnackbar } from 'notistack';
import * as monaco_editor from 'monaco-editor';
import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';


import { FlowAstAutocomplete, toLowerCamelCase, executeTemplate } from './api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


const SelectTask: React.FC<{ value:HdesApi.Entity<HdesApi.AstBody>, onClick: () => void, linked: boolean }> = ({ value, onClick, linked }) => {
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
  flow: HdesApi.Entity<HdesApi.AstFlow>;
  guided: FlowAstAutocomplete;
  cm: typeof monaco_editor;
}

const AutocompleteTask: React.FC<AutocompleteTaskProps> = ({ onClose, guided, flow, cm }) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { decisions, services } = Composer.useSite();
  const { actions, service } = Composer.useComposer();
  const [name, setName] = React.useState("");
  const [apply, setApply] = React.useState(false);
  const [type, setType] = React.useState<HdesApi.AstBodyType | string>(guided.guided === "decision-task" ? "DT" : "FLOW_TASK");
  const [link, setLink] = React.useState<HdesApi.AstBody>();
  const usedLinks = flow.associations.filter(l => l.id && l.owner).map(l => l.id);
  const usedNames = [...Object.values(decisions).map(d => d.ast?.name), ...Object.values(services).map(d => d.ast?.name)]

  const assets:HdesApi.Entity<HdesApi.AstBody>[] = React.useMemo(() => {
    const target:HdesApi.Entity<HdesApi.AstBody>[] = type === "DT" ? Object.values(decisions) : Object.values(services);
    const keyword = name.toLowerCase();
    const result:HdesApi.Entity<HdesApi.AstBody>[] = target.filter(t => t.ast && (
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
      enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.creating" values={{ name: serviceName, type }} />,
        { variant: 'info' }
      );      
      
      service.create().decision(serviceName).then(newSite => {
        enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.created" values={{ name: serviceName, type }}/>,
          { variant: 'success' }
        );
                
        const newAsset = Object.values(newSite.services).filter(a => a.ast?.name === serviceName);
        if (newAsset.length === 1) {
          executeTemplate(cm, toBeReplaced, guided, newAsset[0].ast);
        }
        actions.handleLoadSite(newSite);
        onClose();
      });
    } else if (type === "FLOW_TASK") {
      const serviceName = toBeReplaced.name.charAt(0).toUpperCase() + toBeReplaced.name.slice(1);
      enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.creating" values={{name: serviceName, type}}/>,
        { variant: 'info' }
      );
      service.create().service(serviceName).then(newSite => {
        enqueueSnackbar(<FormattedMessage id="flows.autocomplete.task.snackbar.created" values={{name: serviceName, type}}/>,
          { variant: 'success' }
        );
        
        const newAsset = Object.values(newSite.services).filter(a => a.ast?.name === serviceName);
        if (newAsset.length === 1) {
          executeTemplate(cm, toBeReplaced, guided, newAsset[0].ast);
        }
        actions.handleLoadSite(newSite);
        onClose();
      });
    }
  }

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='flows.autocomplete.task.create' /></DialogTitle>
    <DialogContent>
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
          setName((a.entity.ast as HdesApi.AstBody).name);
        }} />)}

      </List>
    </DialogContent>
    <DialogActions>
      <Button variant='text' disabled={usedNames.includes(name) || name.trim().length === 0 || apply || link ? true : false } onClick={handleSave}>
          <FormattedMessage id="flows.autocomplete.task.create" />
      </Button>
      <CancelButton onClick={onClose} />
      <Button disabled={(link ? false : true) || apply} onClick={handleSave}>
        <FormattedMessage id='flows.autocomplete.task.link'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export { AutocompleteTask };
