import React from 'react';
import { Typography, Grid } from '@mui/material';

import * as colors from 'components-colors';
import { useAm } from 'descriptor-access-mgmt';
import { LayoutList, NavigationButton, LayoutListItem, FilterByString } from 'components-generic';
import { PrincipalsOverviewProvider, useActivePrincipal } from './PrincipalsOverviewContext';

const color_create_permission = colors.steelblue;

const PrincipalsNavigation: React.FC<{}> = () => {

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) { }

  return (<>
    <FilterByString onChange={handleSearch} />

    <NavigationButton id='permissions.navButton.permission.create'
      values={{}}
      color={color_create_permission}
      active={false}
      onClick={() => { }} />
  </>);
}

const PrincipalItems: React.FC = () => {
  const { principals } = useAm();
  const { setActivePrincipal, principalId } = useActivePrincipal();

  if (!principals) {
    return (<>no users defined</>);
  }

  return (<>
    {principals.map((principal, index) =>
      <LayoutListItem active={principalId === principal.id} index={index} key={index} onClick={() => setActivePrincipal(principal.id)}>
        <Grid item lg={5}>
          <Typography>{principal.name}</Typography>
        </Grid>

        <Grid item lg={5}>
          <Typography>{principal.email}</Typography>
        </Grid>

        <Grid item lg={2}>
          <Typography>{principal.status}</Typography>
        </Grid>
      </LayoutListItem>)}
  </>)
}

const PrincipalsOverview: React.FC = () => {
  const navigation = <PrincipalsNavigation />;
  const pagination = <></>;
  const active = <>ACTIVE</>;
  const items = <PrincipalItems />;

  return (
    <PrincipalsOverviewProvider>
      <LayoutList slots={{ navigation, active, items, pagination }} />
    </PrincipalsOverviewProvider>
  )

}

export { PrincipalsOverview };

