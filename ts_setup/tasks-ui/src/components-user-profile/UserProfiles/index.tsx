import React from 'react';
import { Box, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import Backend from 'descriptor-backend';
import { NavigationSticky, FilterByString, useToggle } from 'components-generic';
import { UserProfileDescriptor, ImmutableAmStore, ImmutableUserProfileDescriptor } from 'descriptor-access-mgmt';

import { XTableHead, XPagination, XPaper, XPaperTitleTypography, XTable, XTableBody, XTableBodyCell, XTableHeader, XTableRow } from 'components-xtable';

import Pagination from 'table';

import SelectedUserProfileDialog from './SelectedUserProfileDialog';

export type UserProfilePagination = Pagination.TablePagination<UserProfileDescriptor>;


const UserProfiles: React.FC<{}> = () => {
  const backend = Backend.useBackend();
  const toggle = useToggle<UserProfileDescriptor>();
  const [searchString, setSearchString] = React.useState('');
  const [content, setContent] = React.useState<UserProfilePagination>(new Pagination.TablePaginationImpl<UserProfileDescriptor>({
    src: [],
    orderBy: 'displayName',
    sorted: false
  }));

  React.useEffect(() => {
    const isSearchStringValid: boolean = searchString.trim().length > 2;

    new ImmutableAmStore(backend.store).findAllUserProfiles().then(newRecords => {
      const src = newRecords
        .map(profile => new ImmutableUserProfileDescriptor(profile))
        .filter(profile => {
          if (!isSearchStringValid) {
            return true;
          }
          return profile.displayName.toLowerCase().indexOf(searchString.toLowerCase()) > -1
        });
      setContent((c: UserProfilePagination) => c.withSrc(src));
    });
  }, [searchString]);

  function setStoring(key: string, _direction: string) {
    setContent(prev => prev.withOrderBy(key as (keyof UserProfileDescriptor)));
  }

  return (<>
    <SelectedUserProfileDialog open={toggle.open} profile={toggle.entity} onClose={toggle.handleEnd} />
    <NavigationSticky>
      <FilterByString onChange={({ target }) => setSearchString(target.value)} />
    </NavigationSticky>

    <Box p={1}>
      <XPaper color={""} uuid={`UserProfileSearch.Table`}>
        <XPaperTitleTypography>
          <FormattedMessage id='userprofileTable.header.spotlight.results' />
        </XPaperTitleTypography>

        <XTable columns={3} rows={content.rowsPerPage}>
          <XTableHead>
            <XTableRow>
              <XTableHeader onSort={setStoring} id='displayName' defaultSort='asc'><FormattedMessage id='userprofileTable.header.displayName' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='email'><FormattedMessage id='userprofileTable.header.email' /></XTableHeader>
              <XTableHeader onSort={setStoring} id='created'><FormattedMessage id='userprofileTable.header.created' /></XTableHeader>
            </XTableRow>
          </XTableHead>
          <XTableBody padding={1}>
            {content.entries.map((row) => (
              <XTableRow key={row.id}>
                <XTableBodyCell id="displayName" justifyContent='left' maxWidth={"200px"}>
                  <Button variant='text' onClick={() => toggle.handleStart(row)}>{row.displayName}</Button>
                </XTableBodyCell>
                <XTableBodyCell id="email">
                  {row.email}
                </XTableBodyCell>
                <XTableBodyCell id="created">
                  <Burger.DateTimeFormatter value={row.created} type='dateTime' />
                </XTableBodyCell>
              </XTableRow>))
            }
          </XTableBody>
        </XTable>
        <XPagination state={content} setState={setContent} />
      </XPaper>
    </Box>
  </>
  );
}


export default UserProfiles;