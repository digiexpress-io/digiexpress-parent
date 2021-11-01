import React from 'react';

import { makeStyles, createStyles } from '@mui/styles';
import {
  Theme, Typography, Table, TableBody, TableCell, ButtonGroup,
  TableContainer, TableRow, TableHead, Paper, IconButton,
  AppBar, Toolbar, Button
} from '@mui/material';

import AddIcon from '@mui/icons-material/AddOutlined';
import EditOutlined from '@mui/icons-material/EditOutlined';
import AddLinkIcon from '@mui/icons-material/AddLink';

import { FormattedMessage } from 'react-intl';

import { ArticleLinksEdit } from '../article/ArticleLinksEdit';
import { LinkEdit } from './LinkEdit';
import { LinkComposer } from './LinkComposer';

import { API, Ide } from '../../deps';

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    table: {
      minWidth: 650,
    },
    titleBox: {
      display: 'flex',
      justifyContent: 'space-between',
      backgroundColor: theme.palette.link.main,
      color: theme.palette.link.contrastText
    },
    iconButton: {
      padding: 2,
      marginRight: 2,
      color: theme.palette.link.main,
      "&:hover, &.Mui-focusVisible": {
        backgroundColor: theme.palette.link.main,
        color: theme.palette.link.contrastText,
        "& .MuiSvgIcon-root": {
          color: theme.palette.link.contrastText,
        }
      }
    },
    icon: {
      marginRight: theme.spacing(1)
    },
    bold: {
      fontWeight: 'bold',
    },
    title: {
      paddingLeft: theme.spacing(1),
      color: theme.palette.link.contrastText,
      fontWeight: 'bold'
    },
    tableCell: {
      paddingTop: 0,
      paddingBottom: 0
    },
    button: {
      fontWeight: 'bold',
      color: theme.palette.link.contrastText,
      "&:hover, &.Mui-focusVisible": {
        color: theme.palette.background.paper,
        backgroundColor: theme.palette.link.dark,
        fontWeight: 'bold',
      }
    },
    appBar: {
      position: 'relative',
      backgroundColor: theme.palette.link.main,
      color: theme.palette.secondary.contrastText,
    },

  }));



interface LinkTableProps {
  article: API.CMS.Article,

}

const LinkTable: React.FC<LinkTableProps> = ({ article }) => {
  const classes = useStyles();
  const site = Ide.useSite();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ArticleLinksEdit' | 'LinkEdit' | 'LinkComposer'>(undefined);
  const [link, setLink] = React.useState<undefined | API.CMS.Link>()

  const handleDialogClose = () => {
    setDialogOpen(undefined);
    setLink(undefined);
  }

  const links: API.CMS.Link[] = Object.values(site.links).filter(link => link.body.articles.includes(article.id))
    .sort((o1, o2) => o1.body.value.localeCompare(o2.body.value));

  return (
    <>
      { dialogOpen === 'ArticleLinksEdit' ? <ArticleLinksEdit article={article} articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'LinkEdit' && link ? <LinkEdit link={link} open={true} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'LinkComposer' ? <LinkComposer onClose={handleDialogClose} /> : null}

      <AppBar className={classes.appBar}>
        <Toolbar className={classes.titleBox}>
          <Typography variant="h3" className={classes.title}>{article.body.name}{": "}<FormattedMessage id="links" /></Typography>
          <ButtonGroup>
            <Button variant="text"className={classes.button} autoFocus onClick={() => setDialogOpen("ArticleLinksEdit")}><AddIcon className={classes.icon} />
              <FormattedMessage id='article.links.addremove' /></Button>
            <Button variant="text" className={classes.button} autoFocus onClick={() => setDialogOpen("LinkComposer")}><AddLinkIcon className={classes.icon}/>
              <FormattedMessage id='link.create' /></Button>
          </ButtonGroup>
        </Toolbar>
      </AppBar>
      <TableContainer component={Paper}>
        <Table className={classes.table} size="small" >
          <TableHead>
            <TableRow>
              <TableCell className={classes.bold} align="left"><FormattedMessage id="link.type" /></TableCell>
              <TableCell className={classes.bold} align="left"><FormattedMessage id="value" /></TableCell>
              <TableCell className={classes.bold} align="right" />
            </TableRow>
          </TableHead>
          <TableBody>
            {links.map((link, index) => (
              <TableRow hover key={index}>
                <TableCell className={classes.tableCell} align="left">{link.body.contentType}</TableCell>

                <TableCell className={classes.tableCell} align="left">{link.body.value}</TableCell>
                <TableCell className={classes.tableCell} align="right">
                  <IconButton className={classes.iconButton}><EditOutlined onClick={() => {
                    setLink(link)
                    setDialogOpen('LinkEdit')

                  }} /></IconButton>
                </TableCell>

              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer >
    </>
  );
}

export { LinkTable }




