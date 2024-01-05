import * as React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText } from '@mui/material';
import Check from '@mui/icons-material/Check';
import Client from 'client';
import { FilterByRepoType, FilterBy } from 'descriptor-project';
import { ButtonSearch } from 'components-generic';



const statustypes: Client.RepoType[] = ['DIALOB', 'STENCIL', 'TASKS', 'WRENCH'];

export default function DenseMenu(
  props: {
    onChange: (value: Client.RepoType[]) => void;
    value: FilterBy[]
  }
) {

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  const filterByStatus = props.value.find(filter => filter.type === 'FilterByRepoType') as FilterByRepoType | undefined;

  return (<>
    <ButtonSearch onClick={handleClick} id='project.search.searchBar.filterRepoType' values={{ count: filterByStatus?.repoType.length }} />

    <Menu sx={{ width: 320 }}
      anchorEl={anchorEl}
      open={open}
      onClose={handleClose}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
    >
      <MenuList dense>
        <MenuItem>
          <ListItemText><b>Filter by Repo type</b></ListItemText>
        </MenuItem>
        {statustypes.map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByRepoType');
          const selected = found ? found.type === 'FilterByRepoType' && found.repoType.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}