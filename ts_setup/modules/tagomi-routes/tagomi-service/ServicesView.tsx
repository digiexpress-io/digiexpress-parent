import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl'
import { ServicesViewRoot, useServiceUtilityClasses, ServicesList } from '../tagomi-explorer';


export const ServicesView: React.FC = () => {
  const intl = useIntl();
  const [searchString, setSearchString] = React.useState('')
  const classes = useServiceUtilityClasses();

  return (
    <ServicesViewRoot className={classes.root}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          onChange={(event) => setSearchString(event.target.value.trim())}
          placeholder={intl.formatMessage({ id: 'tagomi.services.searchAll' })}
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
        <ServicesList searchString={searchString} />
    </ServicesViewRoot>)
}


