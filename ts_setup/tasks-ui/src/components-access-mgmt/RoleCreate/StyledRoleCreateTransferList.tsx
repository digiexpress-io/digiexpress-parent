import React from 'react';
import { Box, Typography, Table, TableContainer, TableBody, TableCell, TableRow, TableHead, Paper, IconButton } from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';

import { FormattedMessage, useIntl } from 'react-intl';

import Burger from 'components-burger';
import { cyan } from 'components-colors';

interface StyledTransferListProps {
  title: string;
  titleArgs?: {};
  selectedTitle: string;
  searchTitle: string;
  searchPlaceholder?: string;
  selected: string[];
  headers: string[];

  rows: string[];
  filterRow: (id: string, search: string) => boolean;
  renderCells: (id: string) => (string | React.ReactElement)[];

  cancel: {
    label: string;
    onClick: () => void;
  };

  submit: {
    label: string;
    disabled: boolean;
    onClick: (selected: string[]) => void;
  };
}


const StyledRoleCreateTransferList: React.FC<StyledTransferListProps> = (props) => {
  const { headers, selected: initSelected, searchTitle, selectedTitle, searchPlaceholder, rows, renderCells, filterRow } = props;
  const [search, setSearch] = React.useState("");
  const [searchResult, setSearchResult] = React.useState(rows);
  const [selected, setSelected] = React.useState(initSelected);
  const searchItems = search ? searchResult : rows;
  const intl = useIntl();

  const handleChange = (id: string, command: "add" | "remove") => {
    const currentIndex = selected.indexOf(id);
    const newSelection = [...selected];

    if (selected && currentIndex < 0 && command === "add") {
      newSelection.push(id);
    } else if (currentIndex > -1 && command === "remove") {
      newSelection.splice(currentIndex, 1);
    }
    setSelected(newSelection);
  };


  React.useEffect(() => {
    if (!search) {
      return;
    }
    setSearchResult(rows.filter(row => filterRow(row, search.toLowerCase())));

  }, [search, rows, setSearch, setSearchResult, filterRow]);


  return (
    <>
      <Box sx={{ mt: 1, mb: 1, mr: 3, ml: 3 }}>
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead sx={{ backgroundColor: "table.main" }}>
              <TableRow sx={{ borderBottom: 0 }}>
                <TableCell colSpan={headers.length + 1} sx={{ borderBottom: 0 }}>
                  <Typography variant="body2" sx={{ marginBottom: 1 }}><FormattedMessage id={selectedTitle} /></Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ width: "80px" }} />
                {headers.map((header, index) => (<TableCell key={index} align="left" sx={{ fontWeight: "bold" }}><FormattedMessage id={header} /></TableCell>))}
              </TableRow>
            </TableHead>
            <TableBody>
              {selected.length === 0 ? <TableRow>
                <TableCell colSpan={headers.length + 1}>
                  <Typography variant="body2" sx={{ marginBottom: 1, marginTop: 1 }}><FormattedMessage id="transferlist.noItemsSelected" /></Typography>
                </TableCell>
              </TableRow> : null}

              {selected.map((row, index) => (
                <TableRow hover key={index}>
                  <TableCell>
                    <IconButton sx={{ color: cyan }} onClick={() => handleChange(row, "remove")}>
                      <DeleteOutlineIcon />
                    </IconButton>
                  </TableCell>
                  {renderCells(row).map((cell, cellIndex) => (<TableCell align="left" key={cellIndex}>{cell}</TableCell>))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>

      <Box component={Paper} sx={{ p: 2, pb: 4, mt: 2, mb: 2, mr: 3, ml: 3 }}>
        <Burger.SearchField label={searchTitle} value={search} onChange={setSearch}
          placeholder={intl.formatMessage({ id: searchPlaceholder ? searchPlaceholder : "transferlist.search" })} />
      </Box>

      <Box sx={{ mt: 1, mb: 1, mr: 3, ml: 3 }}>
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead sx={{ backgroundColor: "table.main" }}>
              <TableRow>
                <TableCell sx={{ width: "80px" }} />
                {headers.map((header, index) => (<TableCell key={index} align="left" sx={{ fontWeight: "bold" }}><FormattedMessage id={header} /></TableCell>))}
              </TableRow>
            </TableHead>
            <TableBody>

              {searchItems.length === 0 ? <TableRow>
                <TableCell colSpan={headers.length + 1}>
                  <Typography variant="body2" sx={{ marginBottom: 1, marginTop: 1 }}><FormattedMessage id="transferlist.noSearchResults" /></Typography>
                </TableCell>
              </TableRow> : null}

              {searchItems.map((row, index) => (
                <TableRow hover key={index}>
                  <TableCell>
                    <IconButton sx={{ color: cyan }} onClick={() => handleChange(row, "add")} disabled={selected.includes(row)}>
                      <AddCircleOutlineIcon />
                    </IconButton>
                  </TableCell>
                  {renderCells(row).map((cell, cellIndex) => (<TableCell align="left" key={cellIndex}>{cell}</TableCell>))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>
    </>
  );
}

export type { StyledTransferListProps }
export { StyledRoleCreateTransferList }

