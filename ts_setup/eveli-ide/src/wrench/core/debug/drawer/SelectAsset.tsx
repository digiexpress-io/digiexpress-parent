import React from 'react'

import { Box, List, ListItem, ListItemText, Typography, Divider, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl'
import * as Burger from '@/burger';

import { Composer } from '../../context';
import { HdesApi } from '@/burger';

const SelectTask: React.FC<{ value: HdesApi.Entity<HdesApi.AstBody>, onClick: () => void, linked: boolean }> = ({ value, onClick, linked }) => {
  const { ast } = value;
  if (!ast) {
    return null;
  }

  return (<>
    <ListItem alignItems="flex-start" sx={{ cursor: "pointer" }} onClick={onClick}>
      <ListItemText
        primary={`${linked ? '* ' : ''}${ast.name} - ${value.status}`}
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

interface SelectAssetProps {
  selected?: string;
  onClose: () => void;
  onSelect: (entity: HdesApi.Entity<HdesApi.AstBody>) => void;
}

const SelectAsset: React.FC<SelectAssetProps> = ({ onClose, onSelect, selected }) => {
  const intl = useIntl();
  const { decisions, services, flows } = Composer.useSite();
  const [name, setName] = React.useState("");
  const [type, setType] = React.useState<HdesApi.AstBodyType>("DT");
  const [link, setLink] = React.useState<HdesApi.Entity<HdesApi.AstBody>>();

  const assets: HdesApi.Entity<HdesApi.AstBody>[] = React.useMemo(() => {

    const getValues = () => {
      if (type === "DT") {
        return Object.values(decisions);
      } else if (type === 'FLOW_TASK') {
        return Object.values(services);
      }
      return Object.values(flows);

    }

    const target: HdesApi.Entity<HdesApi.AstBody>[] = getValues();
    const keyword = name.toLowerCase();
    const result: HdesApi.Entity<HdesApi.AstBody>[] = target.filter(t => t.ast && (
      t.ast?.name.toLowerCase().indexOf(keyword) > -1 ||
      (t.ast?.description && t.ast?.description?.toLowerCase().indexOf(keyword) > -1)));
    return result;
  }, [name, type, services, decisions]);






  return (
    <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='debug.select.title' /></DialogTitle>
    <DialogContent>
      <Burger.Select
        selected={type}
        onChange={(newType) => {
          setLink(undefined);
          setType(newType as any);
        }}
        label='debug.select.assetType'
        items={[
          { id: "DT", value: "Decision tables" },
          { id: "FLOW_TASK", value: "Services" },
          { id: "FLOW", value: "Flows" }
        ]}
      />
      <Burger.TextField
        label='debug.select.searchField'
        placeholder={intl.formatMessage({ id: 'debug.select.searchPlaceholder' })}
        helperText='debug.select.searchHelper'
        value={name} onChange={(newName) => {
          if (link) {
            setLink(undefined);
          }
          setName(newName);
        }} />

      <Box pt={2} pb={2}>
        <Typography variant="h4" fontWeight="bold"><FormattedMessage id={"debug.select.searchResults"} /></Typography>
      </Box>
      <List sx={{ width: '100%', height: 400, bgcolor: 'background.paper', overflow: "auto" }}>
        {assets.map(a => {
          const linked = selected === a.id;
          const comp = linked + '-' + a.ast?.name;
          return { entity: a, linked, comp };
        }).sort((a, b) => a.comp.localeCompare(b.comp))
          .map(a => <SelectTask key={a.entity.id} value={a.entity} linked={a.linked} onClick={() => {
            setLink(a.entity);
            setName((a.entity.ast as HdesApi.AstBody).name);
          }} />)}
      </List>
    </DialogContent>
    <DialogActions>
      <Button variant='text' onClick={onClose}>
        <FormattedMessage id='button.cancel'/>
      </Button>
      <Button onClick={() => {
          if(link) {
            onSelect(link);
          }
        }} disabled={link ? false : true}>
        <FormattedMessage id='debug.select.confirm'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export { SelectAsset };
