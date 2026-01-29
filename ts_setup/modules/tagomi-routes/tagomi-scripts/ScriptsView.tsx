import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl'

import { ScriptsViewRoot, useUtilityClasses } from '../tagomi-explorer/script/useUtilityClasses';
import { ScriptsList } from '../tagomi-explorer/script/ScriptsList';



export const ScriptsView: React.FC = () => {
  const intl = useIntl();
  const [searchString, setSearchString] = React.useState('')
  const classes = useUtilityClasses();

  return (
    <ScriptsViewRoot className={classes.root}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          onChange={(event) => setSearchString(event.target.value.trim())}
          placeholder={intl.formatMessage({ id: 'tagomi.scripts.searchAll' })}
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
      <ScriptsList searchString={searchString} />
    </ScriptsViewRoot>)
}


