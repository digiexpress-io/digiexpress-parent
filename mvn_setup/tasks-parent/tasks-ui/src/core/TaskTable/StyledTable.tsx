import { TableCell, TableBody, TableCellProps, TableRow, styled, LinearProgress, Box } from '@mui/material';

const lineHeight = 28;
const lineHeightLarge = 60;


function getTRBackgroundColor(index: number): string {
  const isEven = index % 2 === 0;
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return 'uiElements.light';
  }
  else if (isEven) {
    return 'background.paper';
  }
  else {
    return 'yellow'
  }
}


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
    & tr:last-child td:first-of-type {
      border-radius: 0px 0px 0px 8px;
    }
    & tr:last-child td:last-child {
      border-radius: 0px 0px 8px 0px;
    }
  `};
`;

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



const StyledFillerRows: React.FC<{
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


const StyledLinearProgress: React.FC<{}> = () => {
  return (<Box sx={{ width: '100%' }}><LinearProgress color='primary' /></Box>);
}




export { StyledTableBody, StyledTableCell, StyledFillerRows, lineHeight, lineHeightLarge, getTRBackgroundColor };


