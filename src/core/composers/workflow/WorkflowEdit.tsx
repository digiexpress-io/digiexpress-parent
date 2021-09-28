import React from 'react';
import { createStyles, makeStyles } from '@mui/styles';
import {
  Theme, TextField, InputLabel, FormControl, MenuItem, Select,
  Button, Dialog, DialogTitle, DialogContent, DialogActions, Checkbox, ListItemText,
  ButtonGroup
} from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { API, Ide } from '../../deps';



const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    title: {
      backgroundColor: theme.palette.workflow.main,
      color: theme.palette.secondary.contrastText,
    },
    select: {
      padding: theme.spacing(1),
      marginTop: theme.spacing(3),
      color: theme.palette.primary.contrastText,
      backgroundColor: theme.palette.background.paper
    },
    button: {
      fontWeight: 'bold',
      "&:hover, &.Mui-focusVisible": {
        color: theme.palette.workflow.main,
        fontWeight: 'bold',
      }
    },
    buttonGroup: {
      color: theme.palette.workflow.main
    },
  }),
);

interface WorkflowEditProps {
  workflow: API.CMS.Workflow,
  onClose: () => void,
}

const WorkflowEdit: React.FC<WorkflowEditProps> = ({ onClose, workflow }) => {
  const classes = useStyles();
  const ide = Ide.useIde();
  const { site } = ide.session;

  const [articleId, setArticleId] = React.useState<API.CMS.ArticleId[]>(workflow.body.articles);
  const [locale, setLocale] = React.useState(workflow.body.locale);
  const [technicalname, setTechnicalname] = React.useState(workflow.body.content);
  const [name, setName] = React.useState(workflow.body.name);
  const articles: API.CMS.Article[] = ide.session.getArticlesForLocale(locale);

  const handleCreate = () => {
    const entity: API.CMS.WorkflowMutator = { workflowId: workflow.id, locale, name, content: technicalname, articles: articleId };
    ide.service.update().workflow(entity).then(success => {
      console.log(success)
      onClose();
      ide.actions.handleLoadSite();
    })
  }

  const locales: API.CMS.SiteLocale[] = Object.values(site.locales);

  return (
    <Dialog open={true} onClose={onClose} >
      <DialogTitle className={classes.title}><FormattedMessage id='workflow.edit.title' /></DialogTitle>
      <DialogContent>


          <TextField className={classes.select}
            label={<FormattedMessage id='workflow.composer.technicalname' />}
            helperText={<FormattedMessage id='workflow.composer.technicalname' />}
            variant="outlined"
            fullWidth
            value={technicalname}
            onChange={({ target }) => setTechnicalname(target.value)} />

          <FormControl variant="outlined" className={classes.select} fullWidth>
            <InputLabel><FormattedMessage id='locale' /></InputLabel>
            <Select onChange={({ target }) => {
              const locale: API.CMS.LocaleId = target.value as any;
              if (articleId) {
                const newArticleId = [...articleId]
                const articlesForNewLocale = ide.session.getArticlesForLocale(locale).map(article => article.id);
                for (const nextId of articleId) {
                  if (!articlesForNewLocale.includes(nextId)) {
                    const index = newArticleId.indexOf(nextId);
                    newArticleId.splice(index, 1);
                  }
                }
                setArticleId(newArticleId);
              }
              setLocale(target.value as any);
            }}
              value={locale}
              label={<FormattedMessage id='locale' />}
            >
              {locales.map((locale, index) => (
                <MenuItem key={index} value={locale.id}>{locale.body.value}</MenuItem>
              ))}
            </Select>

          </FormControl>
          <TextField className={classes.select}
            label={<FormattedMessage id='workflow.composer.name' />}
            helperText={<FormattedMessage id='workflow.composer.helper' />}
            variant="outlined"
            fullWidth
            value={name}
            onChange={({ target }) => setName(target.value)} />

        <FormControl variant="outlined" className={classes.select} fullWidth>
          <InputLabel><FormattedMessage id='workflow.composer.select.article' /></InputLabel>
          <Select
            multiline
            multiple
            disabled={!locale}
            onChange={({ target }) => setArticleId(target.value as API.CMS.ArticleId[])}
            value={articleId}
            label={<FormattedMessage id='workflow.composer.select.article' />}
            renderValue={(selected) => (selected as API.CMS.ArticleId[]).map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}
          >
            {articles.map((article, index) => (

              <MenuItem key={index} value={article.id}>
                <Checkbox checked={articleId.indexOf(article.id) > -1} />
                <ListItemText primary={article.body.name} />

              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </DialogContent>

      <DialogActions>
      <ButtonGroup variant="text">
        <Button onClick={onClose} className={classes.button}><FormattedMessage id='button.cancel' /></Button>
        <Button onClick={handleCreate} autoFocus disabled={!name} className={classes.button}><FormattedMessage id='button.add' /></Button>
        </ButtonGroup>
      </DialogActions>
    </Dialog>
  );

}

export { WorkflowEdit }