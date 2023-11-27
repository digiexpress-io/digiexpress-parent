import React from 'react';
import { Stack, Grid, Typography, Alert, Paper, TableContainer, Table, TableHead, TableBody, TableRow, TableCell, Collapse, Box, IconButton } from '@mui/material';

import { emphasize, styled } from '@mui/material/styles';
import Breadcrumbs from '@mui/material/Breadcrumbs';
import Chip from '@mui/material/Chip';
import WrenchOutlinedIcon from '@mui/icons-material/BuildOutlined';
import StencilOutlinedIcon from '@mui/icons-material/AbcOutlined';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

import ConfigItem from './ConfigItem';


const StyledBreadcrumb = styled(Chip)(({ theme }) => {
  const backgroundColor =
    theme.palette.mode === 'light'
      ? theme.palette.grey[100]
      : theme.palette.grey[800];
  return {
    backgroundColor,
    height: theme.spacing(3),
    color: theme.palette.text.primary,
    fontWeight: theme.typography.fontWeightRegular,
    '&:hover, &:focus': {
      backgroundColor: emphasize(backgroundColor, 0.06),
    },
    '&:active': {
      boxShadow: theme.shadows[1],
      backgroundColor: emphasize(backgroundColor, 0.12),
    },
  };
}) as typeof Chip; // TypeScript only: need a type cast here because https://github.com/Microsoft/TypeScript/issues/26591


const SysConfig: React.FC<{}> = () => {


  return (
    <Grid container sx={{ mt: 1, ml: 1 }}>
      <Grid item md={8} lg={8}>
        <Stack sx={{ backgroundColor: 'mainContent.main' }}>

          <Paper sx={{ width: '100%',  p: 3 }}>
            <Breadcrumbs aria-label="breadcrumb">
              <StyledBreadcrumb
                component="a"
                href="#"
                label="Stencil"
                icon={<StencilOutlinedIcon fontSize="small" />}
              />
              <StyledBreadcrumb
                label="LATEST"
                deleteIcon={<ExpandMoreIcon />}
                onDelete={() => { }}
              />

              <StyledBreadcrumb
                component="a"
                href="#"
                label="Wrench"
                icon={<WrenchOutlinedIcon fontSize="small" />}
              />
              <StyledBreadcrumb
                label="LATEST"
                deleteIcon={<ExpandMoreIcon />}
                onDelete={() => { }}
              />

            </Breadcrumbs>
            <Box sx={{pb: 2}}/>
            <ConfigItem />
          </Paper>
        </Stack>
      </Grid>

      <Grid item md={4} lg={4}>
        sdfjkhfk
      </Grid>
    </Grid>

  );
}

export default SysConfig;



