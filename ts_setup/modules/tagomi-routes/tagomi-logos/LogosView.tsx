import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl'

import { LogosViewRoot, useUtilityClasses } from '../tagomi-explorer/logo/useUtilityClasses';
import { LogosList } from '../tagomi-explorer/logo/LogosList';



export const LogosView: React.FC = () => {
  const intl = useIntl();
  const [searchString, setSearchString] = React.useState('')
  const classes = useUtilityClasses();

  return (
    <LogosViewRoot className={classes.root}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          onChange={(event) => setSearchString(event.target.value.trim())}
          placeholder={intl.formatMessage({ id: 'tagomi.logos.searchAll' })}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            },
          }}
        >
          {searchString.trim()}
        </TextField>
      </div>
      <LogosList searchString={searchString} />
    </LogosViewRoot>)
}


