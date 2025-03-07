import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

import { LinksViewRoot, useUtilityClasses} from '../../stencil/explorer/link/useUtilityClasses';
import { LinksList } from '../../stencil/explorer';




export const LinksView: React.FC = () => {
  const [searchString, setSearchString] = React.useState('')
  const classes = useUtilityClasses();


  return (
    <LinksViewRoot className={classes.root}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          onChange={(event) => setSearchString(event.target.value.trim())}
          placeholder='Search Links'
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
          {searchString}
        </TextField>
      </div>
      <LinksList searchString={searchString} />
    </LinksViewRoot>)
}


