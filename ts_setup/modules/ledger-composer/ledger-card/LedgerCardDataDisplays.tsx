import React from 'react';
import { useTheme, Divider, Grid2, Typography, List, ListItem, ListItemText, Box, Collapse, IconButton, alpha } from "@mui/material";
import { CardStyleDefinition, useCardThemeConfig } from "./cardThemeConfig";
import { useIntl } from 'react-intl';
import { ExpandLess as ExpandLessIcon, ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import { useCardConfig } from './CardConfigContext';


interface LedgerCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
}

export const LedgerCardDataRowText: React.FC<LedgerCardDataRowTextProps> = ({ label, value }) => {
  const theme = useTheme();
  const styleConfig = useCardThemeConfig();
  const { cardTheme } = useCardConfig();
  const style = styleConfig[cardTheme];

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
  rows: Record<string, React.ReactNode>[];
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
      {rows.map((row, i) => (
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
            {columns.map((col, index) => (
              <ListItemText
                key={index}
                sx={{ width: col.width ?? `${100 / columns.length}%` }}
              >
                {row[col.key]}
              </ListItemText>
            ))}
          </ListItem>
        </List>
      ))}
      </>
    );
  };

export const LedgerCardDataListExpander: React.FC<{
  columns: { key: string; label: string; width?: string }[];
  rows: Record<string, React.ReactNode>[];
  expanderContent: React.ReactNode[];
}> = ({ columns, rows, expanderContent }) => {
  const theme = useTheme();
  const [expandedRows, setExpandedRows] = React.useState<{ [key: number]: boolean }>({});

  const toggleRow = (index: number) => {
    setExpandedRows(prev => ({ ...prev, [index]: !prev[index] }));
  };

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
      {rows.map((row, i) => (
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
            <IconButton size="small" onClick={() => toggleRow(i)} sx={{ mr: 1 }}>
              {expandedRows[i] ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </IconButton>

            {/* Render row cells */}
            {columns.map((col, i) => (
              <ListItemText key={i} sx={{ width: col.width ?? `${100 / columns.length}%` }}>
                {row[col.key] ?? "-"}
              </ListItemText>
            ))}
          </ListItem>

          {/* Collapsible content */}
          {expanderContent[i] && (
            <Collapse in={expandedRows[i]} timeout="auto" unmountOnExit>
              <Box sx={{
                paddingLeft: theme.spacing(1),
                paddingTop: theme.spacing(1),
                paddingBottom: theme.spacing(1),
                marginLeft: theme.spacing(5),
                borderLeft: `2px solid ${alpha(theme.palette.primary.main, 0.8)}`,
                backgroundColor: alpha(theme.palette.primary.main, 0.05)
              }}
              >
                <Typography variant='subtitle2'>
                  {expanderContent[i]}
                </Typography>
              </Box>
            </Collapse>
          )}
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
