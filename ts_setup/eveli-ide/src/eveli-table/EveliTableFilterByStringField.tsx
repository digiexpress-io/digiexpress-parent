import React from 'react';
import { generateUtilityClass, InputAdornment, styled, TextField, Typography } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import composeClasses from '@mui/utils/composeClasses';

import { useIntl } from 'react-intl';


interface EveliTableFilterByStringProps {
  title: string;
  value: string | undefined;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export const EveliTableFilterByStringField: React.FC<EveliTableFilterByStringProps> = ({ value, onChange, title }) => {
  const classes = useUtilityClasses();
  const filterValue = value ?? '';
  const intl = useIntl();

  function _disableMaterialUIFocusOnUl(event: React.KeyboardEvent<HTMLInputElement>) {
    event.stopPropagation();
  }

  return (
    <EveliTableSearchFieldRoot className={classes.root}>
      <Typography>{intl.formatMessage({ id: 'eveli.table.menu.filter.filterBy', defaultMessage: 'Filter by ' })}{title}</Typography>
      <TextField placeholder='Search' value={filterValue} onChange={onChange} onKeyDown={_disableMaterialUIFocusOnUl}

        slotProps={{
          input: {
            startAdornment: <InputAdornment position="start">
              <SearchIcon className='filters-adornment-icon' />
            </InputAdornment>
          }
        }}
      >
        {filterValue}
      </TextField>
    </EveliTableSearchFieldRoot>
  )
}


export const EveliTableSearchFieldRootClassName = 'EveliTableSearchField';

export const EveliTableSearchFieldRoot = styled('div', {
  name: EveliTableSearchFieldRootClassName,
  slot: 'Search',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {
  return {
    '.MuiFormControl-root': {
      margin: theme.spacing(1),
    },
    '.MuiInputBase-root': {
      paddingLeft: 0,
      fontSize: '10pt',
      height: '2rem',
      minWidth: '100%'
    },
    '.MuiInputBase-input': {
      padding: theme.spacing(0.5),
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
    '.MuiTypography-root': {
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      ...theme.typography.subtitle2,
    }
  }
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableSearchFieldRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}