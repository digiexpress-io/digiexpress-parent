import { TableCell, TableBody, TableCellProps, styled, TableRow, LinearProgress, Box, TableSortLabel } from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';
import Client from '@taskclient';

const lineHeight = 28;
const lineHeightLarge = 60;


const StyledTableBody = styled(TableBody)`
  ${({ theme }) => `
    box-shadow: ${theme.shadows[1]};
    border-top: 2px solid transparent;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-bottom: 8px solid transparent;
    border-radius: 0px 0px 8px 8px;
    background-color: ${theme.palette.background.paper};

    & tr:last-child {
      border-radius: 0px 0px 8px 8px;
    }
        
    & tr:last-child td:first-child {
      border-radius: 0px 0px 0px 8px;
    }
    & tr:last-child td:last-child {
      border-radius: 0px 0px 8px 0px;
    }
  `};
`;

const StyledTableCell = styled(TableCell)<TableCellProps & { rowtype?: 'large'}>(({rowtype, theme}) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  height: (rowtype === 'large' ? lineHeightLarge : lineHeight) + 'px',

  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  
  paddingTop: theme.spacing(0),
  paddingBottom: theme.spacing(0),
}));

const StyledEmptyTableRow: React.FC<{
  content: {emptyRows: number},
  plusColSpan: number,
  loading: boolean
}> = ({ content, loading, plusColSpan }) => {
  if (content.emptyRows === 0) {
    return null;
  }

  const rows: React.ReactNode[] = [];
  for (let index = 0; index < content.emptyRows; index++) {
    rows.push(<TableRow key={index}>
      <StyledTableCell>&nbsp;</StyledTableCell>
      <StyledTableCell colSpan={plusColSpan}>{loading ? <StyledLinearProgress /> : null}</StyledTableCell>
    </TableRow>)
  }
  return (<>{rows}</>);
}


const StyledLinearProgress: React.FC<{  }> = ({  }) => {
  return (<Box sx={{ width: '100%' }}><LinearProgress color='primary' /></Box>);
}


const StyledSortableHeader: React.FC<{
  id: keyof Client.TaskDescriptor,
  content: Client.TablePagination<Client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Client.TablePagination<Client.TaskDescriptor>>>
}> = ({ id, content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof Client.TaskDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableCell key={id} align='left' padding='none' sortDirection={orderBy === id ? order : false}>

      <TableSortLabel active={orderBy === id} direction={orderBy === id ? order : 'asc'} onClick={createSortHandler(id)}>
        <FormattedMessage id={`tasktable.header.${id}`} />
        {orderBy === id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </TableSortLabel>
    </TableCell>
  );
}

export {StyledTableBody, StyledTableCell, lineHeight, lineHeightLarge, StyledEmptyTableRow, StyledSortableHeader };


