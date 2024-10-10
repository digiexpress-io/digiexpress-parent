import React from 'react'

import { Box, List, ListItem, ListItemText, Typography, Divider } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl'
import Burger from '@/burger';

import { Client, Composer } from '../../context';


const SelectTask: React.FC<{ value: Client.Entity<Client.AstBody>, onClick: () => void, linked: boolean }> = ({ value, onClick, linked }) => {
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
  onSelect: (entity: Client.Entity<Client.AstBody>) => void;
}

const SelectAsset: React.FC<SelectAssetProps> = ({ onClose, onSelect, selected }) => {
  const intl = useIntl();
  const { decisions, services, flows } = Composer.useSite();
  const [name, setName] = React.useState("");
  const [type, setType] = React.useState<Client.AstBodyType>("DT");
  const [link, setLink] = React.useState<Client.Entity<Client.AstBody>>();

  const assets: Client.Entity<Client.AstBody>[] = React.useMemo(() => {

    const getValues = () => {
      if (type === "DT") {
        return Object.values(decisions);
      } else if (type === 'FLOW_TASK') {
        return Object.values(services);
      }
      return Object.values(flows);

    }

    const target: Client.Entity<Client.AstBody>[] = getValues();
    const keyword = name.toLowerCase();
    const result: Client.Entity<Client.AstBody>[] = target.filter(t => t.ast && (
      t.ast?.name.toLowerCase().indexOf(keyword) > -1 ||
      (t.ast?.description && t.ast?.description?.toLowerCase().indexOf(keyword) > -1)));
    return result;
  }, [name, type, services, decisions]);



  return (<Burger.Dialog title="debug.select.title" backgroundColor="uiElements.main" open={true} onClose={onClose}
    submit={{
      title: "debug.select.confirm",
      disabled: (link ? false : true),
      onClick: () => {
        if(link) {
          onSelect(link);
        }
      }
    }}>
    <Box>
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
            setName((a.entity.ast as Client.AstBody).name);
          }} />)}
      </List>
    </Box>
  </Burger.Dialog >);
}

export { SelectAsset };
