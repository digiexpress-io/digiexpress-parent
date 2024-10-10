import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TableSortLabel } from "@mui/material";
import React from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { Composer } from "../context";
import { Release } from "./release-types";

type SortOptions = 'name' | 'created';
type SortDirections = 'asc' | 'desc';

interface ReleasesTableProps {
  releases: Release[];
  tableRowComponent: React.FC<{
    release: Release;
  }>;
}

const useSort = (releases: Release[], sort: SortOptions, direction: SortDirections) => {
  const activeBranch = Composer.useBranchName();
  const intl = useIntl();

  const latestRelease = {
    id: 'latest',
    body: {
      name: intl.formatMessage({ id: 'releases.latest.name' }),
      note: intl.formatMessage({ id: 'releases.latest.note' }),
      created: '',
      data: '',
    },
    branches: []
  };

  const defaultBranch = {
    id: 'default',
    body: {
      name: intl.formatMessage({ id: 'releases.default.name' }),
      note: intl.formatMessage({ id: 'releases.default.note' }),
      created: '',
      data: '',
    },
    branches: []
  };

  const defaultBranchRow = activeBranch === undefined ? [] : [defaultBranch];

  switch (sort) {
    case 'name':
      const sortedByName = [...releases].sort((a, b) => {
        const nameA = a.body.name;
        const nameB = b.body.name;
        return (direction === 'asc') ? (nameA.localeCompare(nameB)) : (nameB.localeCompare(nameA));
      });
      return [latestRelease, ...defaultBranchRow, ...sortedByName];
    case 'created':
      const sortedByCreated = [...releases].sort((a, b) => {
        const dateA = new Date(a.body.created);
        const dateB = new Date(b.body.created);
        return (direction === 'asc') ? (dateA.getTime() - dateB.getTime()) : (dateB.getTime() - dateA.getTime());
      });
      return [latestRelease, ...defaultBranchRow, ...sortedByCreated];
    default:
      return [];
  }
};

const ReleasesTable: React.FC<ReleasesTableProps> = ({ releases, tableRowComponent: TableRowComponent }) => {
  const [sort, setSort] = React.useState<SortOptions>('name');
  const [direction, setDirection] = React.useState<SortDirections>('desc');

  const sortByName = () => {
    setSort('name');
    setDirection(direction === 'asc' ? 'desc' : 'asc');
  };

  const sortByCreated = () => {
    setSort('created');
    setDirection(direction === 'asc' ? 'desc' : 'asc');
  };

  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ p: 1 }}>
            <TableCell sx={{ width: "10px" }} />
            <TableCell align="left" sx={{ fontWeight: 'bold' }}>
              <TableSortLabel active={sort === 'name'} direction={direction} onClick={sortByName}>
                <FormattedMessage id="releases.view.tag" />
              </TableSortLabel>
            </TableCell>
            <TableCell sx={{ width: "10px" }} />
            <TableCell align="left" sx={{ fontWeight: 'bold' }}>
              <TableSortLabel active={sort === 'created'} direction={direction} onClick={sortByCreated}>
                <FormattedMessage id="releases.view.created" />
              </TableSortLabel>
            </TableCell>
            <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.note" /></TableCell>
            <TableCell align="center" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.action" /></TableCell>
            <TableCell align="right" sx={{ width: "30px", fontWeight: 'bold' }}><FormattedMessage id="buttons.delete" /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {useSort(releases, sort, direction).map((release, index) => (<TableRowComponent key={index} release={release} />))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

export default ReleasesTable;
