import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useIntl } from 'react-intl';

import { WorkflowsViewRoot, useUtilityClasses } from '../../stencil/explorer/workflow/useUtilityClasses';
import { WorkflowList } from '../../stencil/explorer';


export const WorkflowsView: React.FC = () => {
  const intl = useIntl();
  const [searchString, setSearchString] = React.useState('')
  const classes = useUtilityClasses();


  return (
    <WorkflowsViewRoot className={classes.root}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          onChange={(event) => setSearchString(event.target.value.trim())}
          placeholder={intl.formatMessage({ id: 'services.searchAll' })}
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
      <WorkflowList searchString={searchString} />
    </WorkflowsViewRoot>)
}


