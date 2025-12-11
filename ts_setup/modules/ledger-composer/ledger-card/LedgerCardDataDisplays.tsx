import React from 'react';
import { useTheme, Divider, Grid2, Typography, List, ListItem, ListItemText, Box } from "@mui/material";
import { CardStyleDefinition } from "./cardThemeConfig";
import { useIntl } from 'react-intl';


interface LedgerCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
  style: CardStyleDefinition;
}

export const LedgerCardDataRowText: React.FC<LedgerCardDataRowTextProps> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container margin={theme.spacing(0.5)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography sx={{ ...style.bodyTypography, fontWeight: 500, whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        <Typography sx={{ ...style.bodyTypography, whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {value}
        </Typography>
      </Grid2>
    </Grid2>
    <Divider />
  </>
  )
}

export const LedgerCardDataRowElement: React.FC<{ label: string, value: React.ReactNode, style: CardStyleDefinition }> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container margin={theme.spacing(0.5)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography
          sx={{
            ...style.bodyTypography,
            fontWeight: 500,
            whiteSpace: 'normal',
            wordWrap: 'break-word',
            marginRight: 1
          }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        {value}
      </Grid2>
    </Grid2>
  </>
  )
}
export const LedgerCardDataList: React.FC<{
  columns: { key: string; label: string; width?: string }[];
  rows: React.ReactNode[][];
}> = ({ columns, rows }) => {
  const theme = useTheme();

  return (
    <>
      {/* HEADER */}
      <List dense sx={{ padding: theme.spacing(0.5) }}>
        <ListItem sx={{ display: "flex", paddingLeft: 0, paddingRight: 0 }}>
          {columns.map(col => (
            <Typography key={col.key}
              sx={{
                width: col.width ?? `${100 / columns.length}%`,
                fontWeight: 500
              }}
            >
              {col.label}
            </Typography>
          ))}
        </ListItem>
      </List>

      {/* ROWS */}
      {rows.map((cells, i) => (
        <List dense sx={{ padding: theme.spacing(0.5) }} key={i}>
          <ListItem dense
            sx={{
              display: "flex",
              paddingLeft: 0,
              paddingRight: 0,
              backgroundColor:
                i % 2 === 0
                  ? theme.palette.background.paper
                  : theme.palette.action.hover
            }}
          >
            {cells.map((cell, idx) => (
              <ListItemText key={idx} sx={{ width: columns[idx]?.width ?? `${100 / columns.length}%`}}>
                {cell}
              </ListItemText>
            ))}
          </ListItem>
        </List>
      ))}
    </>
  );
};

export const LedgerCardTransitivesRow: React.FC<{ createdAt: string, updatedAt: string }> = ({ createdAt, updatedAt }) => {
  const intl = useIntl();
  const theme = useTheme();

  return (
    <Box display='flex' gap={theme.spacing(1)} marginTop={theme.spacing(1)} justifyContent='end'>
      <Typography variant='caption'>{intl.formatMessage({ id: 'ledgercard.transitives.createdAt' })}{": "}{createdAt}</Typography>
      <Typography variant='caption'>{intl.formatMessage({ id: 'ledgercard.transitives.updatedAt' })}{": "}{updatedAt}</Typography>
    </Box>
  )
}
