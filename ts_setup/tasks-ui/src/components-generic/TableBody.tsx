import { TableBody, styled } from '@mui/material';

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
export default StyledTableBody;