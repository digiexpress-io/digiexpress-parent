import React from 'react';
import { ListItemText, Box, Typography, } from '@mui/material';
import { useSnackbar } from 'notistack';

import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import { Composer, StencilClient } from '../context';
import { LocaleLabels } from '../locale';

const selectSub = { ml: 2, color: "article.dark" }

const linkTypes: StencilClient.LinkType[] = ["internal", "external", "phone"];

interface LinkEditProps {
  linkId: StencilClient.LinkId,
  onClose: () => void,
}

const LinkEdit: React.FC<LinkEditProps> = ({ linkId, onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const link = site.links[linkId];

  const [value, setValue] = React.useState(link.body.value);
  const [labels, setLabels] = React.useState(link.body.labels);
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const [contentType, setContentType] = React.useState(link.body.contentType);

  const [articleId, setArticleId] = React.useState<StencilClient.ArticleId[]>(link.body.articles);
  //const locales = labels.map(l => l.locale);
  //const articles: StencilClient.Article[] = locales ? session.getArticlesForLocales(locales) : Object.values(site.articles);

  const [devMode, setDevMode] = React.useState(link.body.devMode);


  const handleUpdate = () => {
    const entity: StencilClient.LinkMutator = { linkId: link.id, type: contentType, articles: articleId, labels, value, devMode };
    console.log("entity", entity)
    service.update().link(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    });
  }
  const message = <FormattedMessage id="snack.link.editedMessage" />

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

  return (<Burger.Dialog open={true} onClose={onClose}
    backgroundColor="uiElements.main"
    title="link.edit.title"
    submit={{ title: "button.update", onClick: handleUpdate, disabled: !value || changeInProgress || labels.length < 1 }}>
    <>
      <LocaleLabels
        onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
        onChangeStart={() => setChangeInProgress(true)}
        selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />

      <Burger.Select label="link.type"
        selected={contentType}
        onChange={setContentType as any}
        items={linkTypes.map(link => ({ id: link, value: link }))}
      />

      <Burger.TextField label="link.content" helperText="link.composer.valuehelper" placeholder={link.body.value}
        required
        value={value}
        onChange={setValue} />

      <Box display="flex" alignItems="center" sx={{ mt: 1, mb: 1 }}>
        <Burger.SecondaryButton label={"allarticles"} onClick={() => setArticleId(Object.keys(site.articles))} />
        <Burger.SecondaryButton label={"allarticles.individual"} onClick={() => setArticleId([])} />
        <WarningAmberRoundedIcon sx={{ ml: 3, color: "warning.main" }} /><Typography variant="caption" sx={{ ml: 1 }}><FormattedMessage id="add.allarticles.link.help" /></Typography>
      </Box>

      <Burger.SelectMultiple label='link.article.select' multiline
        selected={articleId}
        onChange={setArticleId}
        renderValue={(selected: StencilClient.ArticleId[]) => selected.map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}
        items={articles.map((article) => ({
          id: article.id,
          value: (<>
            <Burger.Checkbox checked={articleId.indexOf(article.id) > -1}

            />
            <ListItemText primary={article.value} />
          </>)
        }
        ))}
      />
      <Box maxWidth="50%" sx={{ ml: 1 }}>
        <Burger.Switch
          checked={devMode ? devMode : false}
          helperText="link.devmode.helper"
          label="link.devmode"
          onChange={setDevMode}
        />
      </Box>
    </>
  </Burger.Dialog>
  );
}
export { LinkEdit }
