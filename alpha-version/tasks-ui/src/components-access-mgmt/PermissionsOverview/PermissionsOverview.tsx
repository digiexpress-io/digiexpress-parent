import React from 'react';
import { Box, Button } from '@mui/material';

import * as colors from 'components-colors';
import { NavigationButton, FilterByString, NavigationSticky, useToggle } from 'components-generic';
import { Permission, useAm } from 'descriptor-access-mgmt';
import { XTableHead, XPaper, XPaperTitleTypography, XTable, XTableBody, XTableBodyCell, XTableHeader, XTableRow } from 'components-xtable';
import Table from 'table';

import { FormattedMessage } from 'react-intl';
import { PermissionEditDialog } from '../PermissionEdit';

const color_create_permission = colors.steelblue;

const PermissionsNavigation: React.FC<{}> = () => {
  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) { }

  return (<NavigationSticky>
    <FilterByString onChange={handleSearch} />

    <NavigationButton id='permissions.navButton.permission.create'
      values={{}}
      color={color_create_permission}
      active={false}
      onClick={() => { }} />
  </NavigationSticky>);
}

const PermissionItems: React.FC = () => {
  const { permissions } = useAm();
  const editPermission = useToggle<Permission>();
  
  const [content, setContent] = React.useState(new Table.TablePaginationImpl<Permission>({
    src: [],
    orderBy: 'name',
    sorted: false
  }).withRowsPerPage(permissions.length));

  React.useEffect(() => setContent((c) => c.withSrc(permissions)), [permissions]);

  if (!permissions) {
    return (<>no permissions defined</>);
  }
  function setStoring(key: string, _direction: string) {
    setContent(prev => prev
      .withOrderBy(key as (keyof Permission))
      .withRowsPerPage(permissions.length));
  }

  return (<>
    {editPermission.entity && <PermissionEditDialog open={editPermission.open} onClose={editPermission.handleEnd} permission={editPermission.entity} /> }
    <Box p={1}>
      <XPaper color={""} uuid={`PermissionsSearch.Table`}>
        <XPaperTitleTypography>
          <FormattedMessage id='permissions.search.title' />
        </XPaperTitleTypography>

        <XTable columns={3} rows={permissions.length}>
          <XTableHead>
            <XTableRow>
              <XTableHeader onSort={setStoring} id='name' defaultSort='asc'><FormattedMessage id='permissions.permission.name' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='description'><FormattedMessage id='permissions.permission.description' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='status'><FormattedMessage id='permissions.permission.status' /></XTableHeader>
            </XTableRow>
          </XTableHead>
          <XTableBody padding={1}>
            {content.entries.map((row) => (
              <XTableRow key={row.id}>
                <XTableBodyCell id="name" justifyContent='left' maxWidth={"200px"}>
                  <Button variant='text' onClick={() => editPermission.handleStart(row) }>{row.name}</Button>
                </XTableBodyCell>
                <XTableBodyCell id="description">
                  {row.description}
                </XTableBodyCell>
                <XTableBodyCell id="status">
                  {row.status}
                </XTableBodyCell>
              </XTableRow>))
            }
          </XTableBody>
        </XTable>
      </XPaper>
    </Box>
  </>)
}



const PermissionsOverview: React.FC<{}> = () => {
  return (
    <>
      <PermissionsNavigation />
      <PermissionItems />
    </>
  );
}

export { PermissionsOverview };

