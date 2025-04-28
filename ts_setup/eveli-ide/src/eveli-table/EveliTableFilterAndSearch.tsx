import * as React from 'react';
import { Badge, generateUtilityClass, IconButton, ListItemIcon, Menu, MenuItem, styled } from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import composeClasses from '@mui/utils/composeClasses';
import { Header } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

import { EveliTableFilterByStringField } from './EveliTableFilterByStringField';


interface EveliTableFilterAndSearchProps {
  header: Header<unknown, unknown>;
}

export const EveliTableFilterAndSearch: React.FC<EveliTableFilterAndSearchProps> = ({ header }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const classes = useUtilityClasses();
  const intl = useIntl();


  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };


  const isString = typeof header.column.getFilterValue() === 'string';
  const filterValueAsString: string | undefined = isString ? header.column.getFilterValue() as string : undefined;
  const filterValueAsArray: string[] = isString ? [] : (header.column.getFilterValue() as string[] ?? []);
  const title: string = header.column.columnDef.header?.toString().toLowerCase() ?? '';

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {

    e.preventDefault();

    header.column.setFilterValue(e.target.value);
  }

  function handleSelectionChange(selected: string) {
    const next = filterValueAsArray.includes(selected) ?
      filterValueAsArray.filter((value) => value !== selected) :
      [...filterValueAsArray, selected];


    header.column.setFilterValue(next);
  }

  function handleClearFilters() {
    header.column.setFilterValue(undefined);
  }

  const almostUniqueValues = header.column.getFacetedUniqueValues();
  const filterItems: string[] = Array.from(new Set(Array.from(almostUniqueValues.keys())
    .map(key => key as (string | string[]))
    .filter(item => item)
    .filter(item => item.length > 0)
    .map(item => {
      if ((typeof item) === 'string') {
        return [item];
      }
      return item;
    })
    .flatMap(items => items)));

  const filterValue = header.column.getFilterValue();
  const isFilterApplied = filterValue !== undefined && (filterValueAsString || filterValueAsArray.length > 0);


  return (
    <EveliTableColumnFilterRoot className={classes.root}>
      <IconButton onClick={handleClick} disableRipple disableFocusRipple>
        {isFilterApplied ? <Badge overlap="circular" variant="dot"><FilterListIcon /></Badge> : <FilterListIcon />}
      </IconButton>

      <StyledMenu anchorEl={anchorEl} open={open} onClose={handleClose} action={() => { }}>

        <EveliTableFilterByStringField onChange={handleChange} value={filterValueAsString} title={title} />

        <MenuItem onClick={handleClearFilters} >
          <ListItemIcon>
            {header.column.getFilterValue() === undefined ? <CheckBoxIcon className='filters-icon' /> : <CheckBoxOutlineBlankIcon className='filters-icon' />}
          </ListItemIcon>
          {intl.formatMessage({ id: 'eveli.table.menu.filter.showAllItems', defaultMessage: 'Show all items ' })}
        </MenuItem>

        {filterItems.map((item, index) => <React.Fragment key={index}>
          <MenuItem onClick={() => handleSelectionChange(item)}>
            <ListItemIcon>
              {filterValueAsArray.includes(item) ? <CheckBoxIcon className='filters-icon' /> : <CheckBoxOutlineBlankIcon className='filters-icon' />}
            </ListItemIcon>
            {item}
          </MenuItem>
        </React.Fragment>
        )}
      </StyledMenu>
    </EveliTableColumnFilterRoot>
  );
}


const MUI_NAME = 'EveliTableColumnFilter';
const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const EveliTableColumnFilterRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {

  return {
    '.MuiIconButton-root': {
      padding: 0,
      '&:hover': {
        backgroundColor: 'transparent',
      }
    },
    '.MuiSvgIcon-root': {
      ':hover': {
        backgroundColor: theme.palette.secondary.dark,
        borderRadius: theme.spacing(0.5)
      }
    },
    '& .MuiBadge-badge': {
      transform: 'translate(5px, 7px)',
      backgroundColor: theme.palette.error.main
    }
  };
});


const StyledMenu = styled(Menu, {
  name: MUI_NAME,
  slot: 'MenuContainer',
  overridesResolver: (_props, styles) => styles.root,
})(({ theme }) => ({
  '& .MuiPaper-root': {
    backgroundColor: 'white',
    borderRadius: theme.spacing(1),
  },
  '& .MuiMenuItem-root': {
    fontSize: '10pt',
    fontWeight: 400
  },
  '.filters-icon': {
    color: theme.palette.primary.main,
    fontSize: 'medium'
  },
  '.filters-adornment-icon': {
    marginLeft: theme.spacing(1),
    color: theme.palette.primary.main,
    fontSize: 'medium',
  },

}));
