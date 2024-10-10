import React from 'react';
import { createStyles, makeStyles } from '@mui/styles';
import { Theme, Avatar, Collapse, Box, Typography } from '@mui/material';
import IconButton from '@mui/material/IconButton';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import EditOutlined from '@mui/icons-material/EditOutlined';
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined';
import { FormattedMessage, useIntl } from 'react-intl';

import { Composer, StencilClient } from '../context';
import { WorkflowDelete } from './WorkflowDelete';
import { WorkflowEdit } from './WorkflowEdit';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    table: {
      minWidth: 650,
    },
    bold: {
      fontWeight: 'bold'
    },
    title: {
      margin: theme.spacing(1),
      color: theme.palette.text.primary
    },
    avatar: {
      alignSelf: "center",
      marginLeft: theme.spacing(1),
      backgroundColor: theme.palette.workflow.main,
      textTransform: 'uppercase'
    }
  }));

const useRowStyles = makeStyles((theme: Theme) =>
  createStyles({
    row: {
      '& > *': {
        borderBottom: 'unset',
        padding: 0
      },
    },
    expandRow: {
      width: "30px"
    },
    bold: {
      fontWeight: 'bold',
      borderBottom: 'unset'
    },
    column: {
      width: '25%',
      fontWeight: 'bold',
      borderBottom: 'unset',
      padding: 0
    },
    tableCell: {
      paddingTop: 0,
      paddingBottom: 0
    },
    iconButton: {
      padding: 2,
      color: theme.palette.workflow.main,
      "&:hover, &.Mui-focusVisible": {
        backgroundColor: theme.palette.workflow.main,
        color: theme.palette.background.paper,
        "& .MuiSvgIcon-root": {
          color: theme.palette.background.paper,
        }
      }
    },
  }));


const WorkflowsView: React.FC<{}> = () => {
  const classes = useStyles();
  const {site} = Composer.useComposer();
  const workflows = Object.values(site.workflows).sort((o1, o2) => o1.body.value.localeCompare(o2.body.value));
  const title = useIntl().formatMessage({ id: "workflows" });

  return (
    <>
      <Box display="flex">
        <Avatar className={classes.avatar}>{title.substring(0, 2)}</Avatar>
        <Typography variant="h3" className={classes.title}><FormattedMessage id="workflows" />: {workflows.length}</Typography>
               <Typography variant="h3" className={classes.title}><FormattedMessage id="DEV mode" />: 10</Typography>
      </Box>

      <TableContainer component={Paper}>
        <Table className={classes.table} size="small">
          <TableHead>
            <TableRow>
              <TableCell className={classes.bold} align="left" colSpan={2}><FormattedMessage id="workflow.technicalname" /></TableCell>
              <TableCell className={classes.bold} align="center"><FormattedMessage id="locale" /></TableCell>
              <TableCell className={classes.bold} align="left"><FormattedMessage id="workflow.composer.name" /></TableCell>
              <TableCell className={classes.bold} align="center"><FormattedMessage id="articles" /></TableCell>
              <TableCell className={classes.bold} align="center" width="10%"></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {workflows.map((workflow, index) => (<Row key={index} site={site} workflow={workflow} />))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}

interface RowProps {
  site: StencilClient.Site,
  workflow: StencilClient.Workflow,

}

const Row: React.FC<RowProps> = ({ site, workflow }) => {
  const classes = useRowStyles();
  const [open, setOpen] = React.useState(false);
  const articles = workflow.body.articles.map(articleId => site.articles[articleId]);
  const [openDialog, setOpenDialog] = React.useState<"WorkflowEdit" | "WorkflowDelete" | undefined>();

  return (
    <>
      {openDialog === 'WorkflowEdit' ? <WorkflowEdit workflowId={workflow.id} onClose={() => setOpenDialog(undefined)} /> : null}
      {openDialog === 'WorkflowDelete' ? <WorkflowDelete workflow={workflow} onClose={() => setOpenDialog(undefined)} /> : null}

      <TableRow key={workflow.id} hover className={classes.row}>
        <TableCell className={classes.expandRow}>
          <IconButton className={classes.iconButton} size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell className={classes.tableCell} align="left">{workflow.body.value}</TableCell>
        <TableCell className={classes.tableCell} align="center">{workflow.body.articles.length}</TableCell>
        <TableCell className={classes.tableCell} align="right">
          <IconButton className={classes.iconButton} onClick={() => setOpenDialog("WorkflowEdit")}>
            <EditOutlined />
          </IconButton>
          <IconButton className={classes.iconButton} onClick={() => setOpenDialog("WorkflowDelete")}>
            <DeleteOutlinedIcon />
          </IconButton>
        </TableCell>
      </TableRow>

      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box margin={2}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell className={classes.column} align="left" style={{ paddingRight: 0 }}><FormattedMessage id="articles" /></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {articles.map((article, id) => (

                    <TableRow hover key={id} className={classes.row}>
                      <TableCell component="th" scope="row" align="left" >
                        {article.body.name}
                      </TableCell>

                      <TableCell align="left">
                        {"hahaha missing to"
                          // <WorkflowRemovePage locale={workflow.body.locale} workflow={workflow} article={article} />
                          
                        }
                      </TableCell>

                    </TableRow>

                  ))}
                </TableBody>
              </Table>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  )
}

export { WorkflowsView }




