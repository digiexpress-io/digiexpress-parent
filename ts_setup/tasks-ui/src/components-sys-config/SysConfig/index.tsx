import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert } from '@mui/material';

import Context from 'context';
import { NavigationSticky, NavigationButton } from 'components-generic';



const SysConfigLoader: React.FC = () => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    if(!loading) {
      return;
    }
    
    
  }, [loading]);


  if (loading) {
    return <>...loading</>
  }
  return (<Grid container>

{/*
      <NavigationSticky>
        {state.tabs.map(tab => (
          <NavigationButton
            id={tab.label}
            values={{ count: tab.count }}
            key={tab.id}
            active={tab.selected}
            color={tab.color}
            onClick={() => handleActiveTab(tab.id)} />
        ))
        }
      </NavigationSticky>
*/}
      <Grid item md={8} lg={8}>
        content
      </Grid>

      <Grid item md={4} lg={4}>
        right bar
      </Grid>
      </Grid>);
}

export default SysConfigLoader;
