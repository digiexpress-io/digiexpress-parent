import React from 'react';
import { Button, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import * as colors from 'components-colors';
import { Principal, useAm } from 'descriptor-access-mgmt';
import { NavigationButton, FilterByString, NavigationSticky, useToggle } from 'components-generic';

import { XTableHead, XPaper, XPaperTitleTypography, XTable, XTableBody, XTableBodyCell, XTableHeader, XTableRow } from 'components-xtable';
import Table from 'table';

import { PrincipalEditDialog } from '../PrincipalEdit';


const color_create_permission = colors.steelblue;

const PrincipalsNavigation: React.FC<{}> = () => {

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) { }

  return (<NavigationSticky>
    <FilterByString onChange={handleSearch} />

    <NavigationButton id='permissions.navButton.principal.create'
      values={{}}
      color={color_create_permission}
      active={false}
      onClick={() => { }} />
  </NavigationSticky>);
}

const PrincipalItems: React.FC = () => {
  const { principals } = useAm();
  const editPrincipal = useToggle<Principal>();

  if (!principals) {
    return (<>no users defined</>);
  }

  const [content, setContent] = React.useState(new Table.TablePaginationImpl<Principal>({
    src: [],
    orderBy: 'name',
    sorted: false
  }).withRowsPerPage(principals.length));

  React.useEffect(() => setContent((c) => c.withSrc(principals)), [principals]);

  if (!principals) {
    return (<>no principals defined</>);
  }
  function setStoring(key: string, _direction: string) {
    setContent(prev => prev
      .withOrderBy(key as (keyof Principal))
      .withRowsPerPage(principals.length));
  }
  return (<>
    {editPrincipal.entity && <PrincipalEditDialog open={editPrincipal.open} onClose={editPrincipal.handleEnd} principal={editPrincipal.entity} />}
    <Box p={1}>
      <XPaper color={""} uuid={`PrincipalSearch.Table`}>
        <XPaperTitleTypography>
          <FormattedMessage id='permissions.editPermission.principals' />
        </XPaperTitleTypography>

        <XTable columns={4} rows={principals.length}>
          <XTableHead>
            <XTableRow>
              <XTableHeader onSort={setStoring} id='name' defaultSort='asc'><FormattedMessage id='permissions.principal.name' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='email'><FormattedMessage id='permissions.principal.email' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='directRoles'><FormattedMessage id='permissions.permission.status' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='directPermissions'><FormattedMessage id='permissions.permission.status' /></XTableHeader>
            </XTableRow>
          </XTableHead>
          <XTableBody padding={1}>
            {content.entries.map((row) => (
              <XTableRow key={row.id}>
                <XTableBodyCell id="name" justifyContent='left' maxWidth={"200px"}>
                  <Button variant='text' onClick={() => editPrincipal.handleStart(row)}>{row.name}</Button>
                </XTableBodyCell>
                <XTableBodyCell id="email">
                  {row.email}
                </XTableBodyCell>
                <XTableBodyCell id="directRoles">
                  {row.directRoles.length ? row.directRoles
                    .map(role => (<Typography variant='caption' key={role}>{role}</Typography>)) : <FormattedMessage id='permissions.principal.directRoles.none' />
                  }
                </XTableBodyCell>
                <XTableBodyCell id="directPermissions">
                  {row.directPermissions.length ? row.directPermissions
                    .map(permission => (<Typography variant='caption' key={permission}>{permission}</Typography>)) : <FormattedMessage id='permissions.principal.directPermissions.none' />
                  }
                </XTableBodyCell>
              </XTableRow>))
            }
          </XTableBody>
        </XTable>
      </XPaper>
    </Box>
  </>)
}


const PrincipalsOverview: React.FC = () => {
  return (
    <>
      <PrincipalsNavigation />
      <PrincipalItems />
    </>
  )

}

export { PrincipalsOverview };

