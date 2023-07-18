import React from 'react';
import { FormattedMessage } from 'react-intl';

import { createStyles, makeStyles } from '@material-ui/styles';
import {
  Theme,
  Table, TableBody, TableCell, TableHead, TableRow, TableContainer,
  Accordion, AccordionDetails, AccordionSummary,
  Toolbar, Typography
} from '@material-ui/core';
import AddCircleIcon from '@material-ui/icons/AddCircle';

import DateFormat from '../date-format';
import Resource from '../';
import { AddRelease } from './AddRelease';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      paddingLeft: theme.spacing(4),
      paddingRight: theme.spacing(4)
    },
    table: {
      width: '100%',
    },
  }),
);

const useToolbarStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(1),
    },
    title: {
      flex: '1 1 100%',
    },
    icon: {
      width: '30px',
      height: '30px',
    },
    accordion: {
      width: '100%',
      backgroundColor: 'transparent',
    }
  }),
);

const EnhancedTableToolbar = () => {
  const classes = useToolbarStyles();


  return (
    <Toolbar>
      <Accordion className={classes.accordion}>
        <AccordionSummary
          expandIcon={<AddCircleIcon className={classes.icon} color="primary" />}
          aria-controls="panel1a-content"
          id="panel1a-header"
        >
          <Typography className={classes.title} variant="h6" component="div">
            <FormattedMessage id="releases.table.name" />
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <AddRelease />
        </AccordionDetails>
      </Accordion>
    </Toolbar>
  );
};

interface ReleasesProps {
};

const Releases: React.FC<ReleasesProps> = () => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const tags = resource.session.models.TAG;
  const items = (tags ? tags : []).map(tag => ({ name: tag.name, created: tag.created, message: tag.description }));

  return (<div className={classes.root}>
    <EnhancedTableToolbar />
    <TableContainer>
      <Table className={classes.table} size="small">
        <TableHead>
          <TableRow>
            <TableCell><FormattedMessage id="releases.table.column.name" /></TableCell>
            <TableCell><FormattedMessage id="releases.table.column.message" /></TableCell>
            <TableCell align="right"><FormattedMessage id="releases.table.column.created" /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((row, index) => (
            <TableRow hover key={index}>
              <TableCell align="left">{row.name}</TableCell>
              <TableCell align="right">{row.message}</TableCell>
              <TableCell align="right"><DateFormat>{row.created}</DateFormat></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>

  </div>);
}

export type { ReleasesProps };
export { Releases };
