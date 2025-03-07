import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

import { ArticleList, useUtilityClasses } from '../explorer/article'
import { ArticlesViewRoot } from '../../stencil/explorer/article/useUtilityClasses';




export const ArticlesView: React.FC = () => {
  const [searchString, setSearchString] = React.useState('')
  const classes = useUtilityClasses();


  return (
    <ArticlesViewRoot className={classes.root}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          onChange={(event) => setSearchString(event.target.value.trim())}
          placeholder='Search Articles'
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
      <ArticleList searchString={searchString} />
    </ArticlesViewRoot>)
}


