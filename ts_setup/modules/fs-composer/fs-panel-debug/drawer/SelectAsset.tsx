import React from 'react'
import { Box, List, ListItem, ListItemText, Typography, Divider, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { useIntl } from 'react-intl'
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentSelectSingle, FsDirentTextField } from '../../fs-utilities';



const flattenDirents = (nodes: Fs.DirentBase[]): Fs.DirentBase[] => {
  const result: Fs.DirentBase[] = [];
  for (const node of nodes) {
    result.push(node);
    if (node.children && node.children.length > 0) {
      result.push(...flattenDirents(node.children));
    }
  }
  return result;
}

const SelectTask: React.FC<{ value: Fs.DirentBase, onClick: () => void, linked: boolean }> = ({ value, onClick, linked }) => {
  return (<>
    <ListItem alignItems="flex-start" sx={{ cursor: "pointer" }} onClick={onClick}>
      <ListItemText
        primary={`${linked ? '* ' : ''}${value.name}`}
        secondary={<Typography
          sx={{ display: 'inline' }}
          component="span"
          variant="body2"
          color="text.primary">
          {value.type}
        </Typography>} />
    </ListItem>
    <Divider />
  </>);
}

interface SelectAssetProps {
  selected?: string;
  onClose: () => void;
  onSelect: (dirent: Fs.DirentBase) => void;
}

const SelectAsset: React.FC<SelectAssetProps> = ({ onClose, onSelect, selected }) => {
  const intl = useIntl();
  const { dirents } = useFsDirent();
  const [name, setName] = React.useState("");
  const [type, setType] = React.useState<Fs.BodyType>("DECISION_TABLE");
  const [link, setLink] = React.useState<Fs.DirentBase | undefined>();

  const assets: Fs.DirentBase[] = React.useMemo(() => {
    const all = flattenDirents(dirents);
    const filtered = all.filter(d => d.type === type);
    const keyword = name.toLowerCase();
    const result = filtered.filter(d =>
      d.name.toLowerCase().indexOf(keyword) > -1
    );
    return result;
  }, [name, type, dirents]);

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'debug.select.title' })}</DialogTitle>
      <DialogContent>
        <FsDirentSelectSingle
          value={type}
          onChange={(newType) => {
            setLink(undefined);
            setType(newType as Fs.BodyType);
          }}
          options={[
            { value: "DECISION_TABLE", label: "Decision tables" },
            { value: "FLOW_TASK", label: "Services" },
            { value: "FLOW", label: "Flows" }
          ]}
        />
        <Box m={1} />
        <FsDirentTextField
          placeholder={intl.formatMessage({ id: 'debug.select.searchPlaceholder' })}
          value={name}
          onChange={(newName) => {
            if (link) {
              setLink(undefined);
            }
            setName(newName);
          }}
        />

        <Box pt={2} pb={2}>
          <Typography variant="h4" fontWeight="bold">{intl.formatMessage({ id: 'debug.select.searchResults' })}</Typography>
        </Box>
        <List sx={{ width: '100%', height: 400, bgcolor: 'background.paper', overflow: "auto" }}>
          {assets.map(a => {
            const linked = selected === a.id;
            const comp = linked + '-' + a.name;
            return { dirent: a, linked, comp };
          }).sort((a, b) => a.comp.localeCompare(b.comp))
            .map(a => <SelectTask key={a.dirent.id} value={a.dirent} linked={a.linked} onClick={() => {
              setLink(a.dirent);
              setName(a.dirent.name);
            }} />)}
        </List>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={() => {
          if (link) {
            onSelect(link);
          }
        }} disabled={link ? false : true}>
          {intl.formatMessage({ id: 'debug.select.confirm' })}
        </Button>
      </DialogActions>
    </Dialog>);
}

export { SelectAsset };
