import React from 'react';
import { ListItemText, Box, Typography, Button, Checkbox, Dialog, DialogTitle, DialogContent, DialogActions, useTheme, Divider, FormHelperText } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage, useIntl } from 'react-intl';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import * as Burger from '@dxs-ts/eveli-primitives';
import { LocaleLabels } from '../stencil-locale';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const selectSub = { ml: 2, color: "article.dark" }

const LinkComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const intl = useIntl();
  const theme = useTheme();
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();

  const [type, setType] = React.useState<'internal' | 'external' | 'phone' | string>('internal');
  const [value, setValue] = React.useState('');
  const [labels, setLabels] = React.useState<StencilApi.LocaleLabel[]>([]);
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const [articleId, setArticleId] = React.useState<StencilApi.ArticleId[]>([]);
  //const articles: StencilApi.Article[] = Object.values(site.articles);
  const [devMode, setDevMode] = React.useState<boolean>(false);

  const handleCreate = () => {
    const entity: StencilApi.CreateLink = { type, value, articles: articleId, labels, devMode };
    service.create().link(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }

  const message = <FormattedMessage id="snack.link.createdMessage" />

  const articles: { id: string, value: string }[] = Object.values(site.articles)
    .sort((a1, a2) => {
      if (a1.body.parentId && a1.body.parentId === a2.body.parentId) {
        const children = a1.body.order - a2.body.order;
        if (children === 0) {
          return a1.body.name.localeCompare(a2.body.name);
        }
        return children;
      }

      return (a1.body.parentId ? site.articles[a1.body.parentId].body.order + 1 : a1.body.order)
        - (a2.body.parentId ? site.articles[a2.body.parentId].body.order + 1 : a2.body.order);
    })
    .map(article => ({
      id: article.id,
      value: `${article.body.order} - ${article.body.parentId ? site.articles[article.body.parentId].body.name + "/" : ""}${article.body.name}`,
      sx: article.body.parentId ? selectSub : undefined
    }));


  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='link.composer.title' /></DialogTitle>
      <DialogContent>
        <LocaleLabels
          onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />

        <Burger.Select label='link.type'
          selected={type}
          onChange={setType}

          items={[
            { id: 'internal', value: <FormattedMessage id='link.type.internal' /> },
            { id: 'external', value: <FormattedMessage id='link.type.external' /> },
            { id: 'phone', value: <FormattedMessage id={'link.type.phone'} /> }
          ]} />

        <Burger.TextField label='value' helperText='link.composer.valuehelper'
          required
          value={value}
          onChange={setValue} />
        {!value && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}

        <Divider sx={{ my: theme.spacing(2) }} />

        <Typography fontWeight='bold'><FormattedMessage id='composer.select.article' /></Typography>
        <Burger.SelectMultiple
          variant='ARTICLE_SELECT'
          label='article.select'
          multiline
          onChange={setArticleId}
          selected={articleId}

          renderValue={(selected: StencilApi.ArticleId[]) => selected.map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}
          items={articles.map(article => ({
            id: article.id,
            value: (<>
              <Checkbox checked={articleId.includes(article.id) ? true : false} />
              <ListItemText primary={article.value} />
            </>)
          })
          )} />


        <Box maxWidth="50%" sx={{ ml: 1 }}>
          <Burger.Switch
            checked={devMode}
            helperText="link.devmode.helper"
            label="link.devmode"
            onChange={setDevMode}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!value || changeInProgress || labels.length < 1}>
          <FormattedMessage id='button.create' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { LinkComposer }