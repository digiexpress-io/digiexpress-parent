import React from 'react';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { FilterByString, NavigationSticky, useToggle } from 'components-generic';
import { XTableHead, XPagination, XPaper, XPaperTitleTypography, XTable, XTableBody, XTableBodyCell, XTableHeader, XTableRow, XPanderProvider, XTableRowCollapse, XPanderButton } from 'components-xtable';

import { CustomerAvatar } from '../CustomerDetails';
import { useCustomerSearchState } from './CustomersSearchState';
import { Box } from '@mui/material';
import { CustomerTasks } from './CustomerTasks';


export const CustomerSearch: React.FC<{}> = () => {
  const { content, setContent, setStoring, setSearchString } = useCustomerSearchState();

  return (
    <>
      <NavigationSticky>
        <FilterByString onChange={({ target }) => setSearchString(target.value)} />
      </NavigationSticky>

      <Box p={1}>
        <XPaper color={""} uuid={`CustomerSearch.Table`}>
          <XPaperTitleTypography>
            <FormattedMessage id={`customertable.header.spotlight.searchResults`} />
          </XPaperTitleTypography>

          <XTable columns={7} rows={content.rowsPerPage}>
            <XTableHead>
              <XTableRow>
                <XTableHeader onSort={setStoring} id='displayName' colSpan={2}><FormattedMessage id='customertable.header.displayName' /></XTableHeader>
                <XTableHeader onSort={setStoring} id='customerType'><FormattedMessage id='customertable.header.customerType' /></XTableHeader>
                <XTableHeader onSort={setStoring} id='created' defaultSort='asc'><FormattedMessage id='customertable.header.created' /></XTableHeader>
                <XTableHeader onSort={setStoring} id='lastLogin'><FormattedMessage id='customertable.header.lastLogin' /></XTableHeader>
              </XTableRow>
            </XTableHead>
            <XTableBody padding={1}>
              {content.entries.map((row) => (
                <XPanderProvider key={row.id}>
                  <XTableRow>
                    <XTableBodyCell id="tasks" justifyContent='left' maxWidth='30px' padding='none'><XPanderButton /></XTableBodyCell>
                    <XTableBodyCell id="displayName" justifyContent='left' maxWidth={"500px"}><CustomerAvatar customerId={row.id} /></XTableBodyCell>
                    <XTableBodyCell id="customerType">{row.customerType}</XTableBodyCell>
                    <XTableBodyCell id="created"><Burger.DateTimeFormatter value={row.created} type='dateTime' /></XTableBodyCell>
                    <XTableBodyCell id="lastLogin"><Burger.DateTimeFormatter value={row.lastLogin} type='dateTime' /></XTableBodyCell>
                  </XTableRow>
                  <XTableRowCollapse><CustomerTasks customerId={row.id} /></XTableRowCollapse>
                </XPanderProvider>))
              }
            </XTableBody>
          </XTable>
          <XPagination state={content} setState={setContent} />
        </XPaper>
      </Box>
    </>);
}