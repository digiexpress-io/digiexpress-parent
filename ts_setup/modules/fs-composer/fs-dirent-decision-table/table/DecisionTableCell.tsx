import React from 'react';
import { Box, Button, ButtonGroup, TableCell, Tooltip, Typography, alpha, darken, lighten, useTheme } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';


const DecisionTableCell: React.FC<{
  row: Fs.DecisionAstRow;
  header: Fs.DecisionTypeDef;
  cell: Fs.DecisionAstCell | undefined;
}> = ({ header, cell }) => {

  const theme = useTheme();
  const borderColor = theme.palette.mode === 'light'
    ? lighten(alpha(theme.palette.divider, 1), 0.88)
    : darken(alpha(theme.palette.divider, 1), 0.68);

  if (header.direction === 'IN') {
    return (
      <TableCell sx={{ borderRight: `1px ${borderColor} solid` }}>
        <Typography noWrap>
          {cell?.value ?? <Box component="span" sx={{ color: 'text.disabled' }}>—</Box>}
        </Typography>
      </TableCell>
    );
  }

  if (header.valueType === 'INTL') {
    return (
      <TableCell sx={{ borderRight: `1px ${borderColor} solid` }}>
        <ButtonGroup variant="text">
          {(header.valueSet ?? []).map((locale) => (
            <Tooltip key={locale} title={cell?.value ?? ''}>
              <Button>
                <Typography textTransform="uppercase" fontWeight="bold">{locale}</Typography>
              </Button>
            </Tooltip>
          ))}
        </ButtonGroup>
      </TableCell>
    );
  }

  return (
    <TableCell sx={{ borderRight: `1px ${borderColor} solid` }}>
      <Typography noWrap>
        {cell?.value ?? <Box component="span" sx={{ color: 'text.disabled' }}>—</Box>}
      </Typography>
    </TableCell>
  );
};

export { DecisionTableCell };
