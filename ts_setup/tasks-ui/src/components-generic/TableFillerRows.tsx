import { TableCell, TableCellProps, TableRow, styled, LinearProgress, Box } from '@mui/material';


const lineHeight = 28;
const lineHeightLarge = 60;

const StyledLinearProgress: React.FC<{}> = () => {
  return (<Box sx={{ width: '100%' }}><LinearProgress color='primary' /></Box>);
}


const StyledTableCell = styled(TableCell)<TableCellProps & { rowtype?: 'large' }>(({ rowtype, theme }) => ({
  textAlign: 'left',
  fontSize: "13px",
  fontWeight: '400',
  height: (rowtype === 'large' ? lineHeightLarge : lineHeight) + 'px',

  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),

  paddingTop: theme.spacing(0),
  paddingBottom: theme.spacing(0),
}));

const TableFillerRows: React.FC<{
  content: { emptyRows: number },
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

export { TableFillerRows, StyledTableCell, lineHeight, lineHeightLarge }