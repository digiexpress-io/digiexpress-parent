import React from 'react';
import { ListItemText, Box, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';

import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import { FormattedMessage } from 'react-intl';

import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';
import { LocaleLabels } from '../locale';

const selectSub = { ml: 2, color: "article.dark" }

const LinkComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();

  const [type, setType] = React.useState<'internal' | 'external' | 'phone' | string>('internal');
  const [value, setValue] = React.useState('');
  const [labels, setLabels] = React.useState<StencilClient.LocaleLabel[]>([]);
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const [articleId, setArticleId] = React.useState<StencilClient.ArticleId[]>([]);
  //const articles: StencilClient.Article[] = Object.values(site.articles);
  const [devMode, setDevMode] = React.useState<boolean>(false);

  const handleCreate = () => {
    const entity: StencilClient.CreateLink = { type, value, articles: articleId, labels, devMode };
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
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="link.composer.title"
      submit={{ title: "button.create", onClick: handleCreate, disabled: !value || changeInProgress || labels.length < 1 }}>

      <>
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

        <Box display="flex" alignItems="center" sx={{ mt: 1, mb: 1 }}>
          <Burger.SecondaryButton label={"allarticles"} onClick={() => setArticleId(Object.keys(site.articles))} />
          <Burger.SecondaryButton label={"allarticles.individual"} onClick={() => setArticleId([])} />
          <WarningAmberRoundedIcon sx={{ ml: 3, color: "warning.main" }} /><Typography variant="caption" sx={{ ml: 1 }}><FormattedMessage id="add.allarticles.link.help" /></Typography>
        </Box>

        <Burger.SelectMultiple label='article.select'
          multiline
          onChange={setArticleId}
          selected={articleId}

          renderValue={(selected: StencilClient.ArticleId[]) => selected.map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}
          items={articles.map(article => ({
            id: article.id,
            value: (<>
              <Burger.Checkbox checked={articleId.includes(article.id) ? true : false} />
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
      </>
    </Burger.Dialog>
  );
}

export { LinkComposer }