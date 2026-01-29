import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl'

//import { ArticleList, useUtilityClasses } from '../stencil-explorer/article'
import { ServicesViewRoot, useUtilityClasses } from '../tagomi-explorer/service/useUtilityClasses';
import { ServicesList } from '../tagomi-explorer/service/ServicesList';



export const ServicesView: React.FC = () => {
  const intl = useIntl();
  const [searchString, setSearchString] = React.useState('')
  const classes = useUtilityClasses();

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


