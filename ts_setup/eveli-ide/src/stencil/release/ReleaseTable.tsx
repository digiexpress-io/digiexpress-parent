import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TableSortLabel, IconButton } from "@mui/material";
import GetAppIcon from '@mui/icons-material/GetApp';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import fileDownload from 'js-file-download'

import React from "react";
import { FormattedMessage } from "react-intl"
import { StencilApi } from '../client'
import { Composer } from '../context';
import { ReleaseDelete } from './ReleaseDelete'
import * as Burger from '@/burger';

interface ReleaseTableProps {
  releases: StencilApi.Release[];
}


const ReleaseTable: React.FC<ReleaseTableProps> = ({ releases }) => {

  type sortOptions = 'name' | 'created';
  type sortDirections = 'asc' | 'desc';
  const [sort, setSort] = React.useState<sortOptions>('name');
  const [direction, setDirection] = React.useState<sortDirections>('desc');

  const sortByParam = (param: sortOptions, dir: sortDirections) => {
    switch (param) {
      case 'name':
        return [...releases].sort((a, b) => {
          const nameA = a.body.name;
          const nameB = b.body.name;
          return (dir === 'asc') ? (nameA.localeCompare(nameB)) : (nameB.localeCompare(nameA));
        });
      case 'created':
        return [...releases].sort((a, b) => {
          const dateA = new Date(a.body.created);
          const dateB = new Date(b.body.created);
          return (dir === 'asc') ? (dateA.getTime() - dateB.getTime()) : (dateB.getTime() - dateA.getTime());
        });
      default:
        return [];
    }
  };

  const sortByName = () => {
    setSort('name');
    setDirection((direction === 'asc') ? 'desc' : 'asc');
  }

  const sortByCreated = () => {
    setSort('created');
    setDirection((direction === 'asc') ? 'desc' : 'asc');
  }

  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ p: 1 }}>
            <TableCell align="left" sx={{ fontWeight: 'bold' }}>
              <TableSortLabel active={sort === 'name'} direction={direction} onClick={() => sortByName()}>
                <FormattedMessage id="releases.view.tag" />
              </TableSortLabel>
            </TableCell>
            <TableCell align="left" sx={{ fontWeight: 'bold' }}>
              <TableSortLabel active={sort === 'created'} direction={direction} onClick={() => sortByCreated()}>
                <FormattedMessage id="releases.view.created" />
              </TableSortLabel>
            </TableCell>
            <TableCell align="left" sx={{ fontWeight: 'bold' }}><FormattedMessage id="releases.view.note" /></TableCell>
            <TableCell align="center"><FormattedMessage id="releases.view.download" /></TableCell>
            <TableCell align="right" sx={{ width: "30px" }}></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {sortByParam(sort, direction).map((release, index) => (<Row key={index} release={release} />))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}


const Row: React.FC<{ release: StencilApi.Release }> = ({ release }) => {
  const [releaseDeleteOpen, setReleaseDeleteOpen] = React.useState(false);
  const { service } = Composer.useComposer();

  const onDownload = (releaseId: string) => {
    service.getReleaseContent(releaseId).then(content => {
      const data = JSON.stringify(content, null, 2);
      fileDownload(data, release.body.name + '.json');
    })  
  }

  return (
    <>
      {releaseDeleteOpen ? <ReleaseDelete id={release.id} onClose={() => setReleaseDeleteOpen(false)} /> : null}

      <TableRow key={release.id}>
        <TableCell align="left" >{release.body.name}</TableCell>
        <TableCell align="left"><Burger.DateTimeFormatter timestamp={release.body.created} /></TableCell>
        <TableCell align="left">{release.body.note}</TableCell>
        <TableCell align="center" >
          <IconButton onClick={() => onDownload(release.id)}><GetAppIcon /> </IconButton>
        </TableCell>
        <TableCell align="center" >
          <IconButton onClick={() => setReleaseDeleteOpen(true)} sx={{ color: 'error.main' }}><DeleteOutlineIcon /> </IconButton>
        </TableCell>
      </TableRow>
    </>
  )
}


export type { ReleaseTableProps };
export { ReleaseTable }
