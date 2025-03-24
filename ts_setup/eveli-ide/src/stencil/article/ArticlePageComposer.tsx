import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, useTheme } from '@mui/material';

import MDEditor, { ICommand, commands, TextState, TextAreaTextApi } from '@uiw/react-md-editor';
import { Composer } from '../context';
import { StencilApi } from '@/burger';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import ArticlePageItem from '../explorer/article/ArticlePageItem';
import { SimpleTreeView } from '@mui/x-tree-view';

const regexp_starts_with = new RegExp('^# .');

const isValidTitle = (value?: string) => {
  if (!value) {
    return false;
  }
  if (regexp_starts_with.test(value)) {
    return true;
  }

  const start = value.indexOf("# ");
  if (start < 0) {
    return false;
  }

  const cleaned = start === 0 ?
    value :
    value.substring(0, start).replaceAll("\n", "") + value.substring(start);
  return regexp_starts_with.test(cleaned);
}

const templateCommand = (template: StencilApi.Template): ICommand => ({
  name: 'templates' + template.id,
  keyCommand: 'templates' + template.id,
  buttonProps: { 'aria-label': template.body.name, title: template.body.name },
  icon: <div style={{ fontSize: 18, padding: 10, textAlign: 'left' }}>{template.body.name} - {template.body.description}</div>,
  execute: (_state: TextState, api: TextAreaTextApi) => {
    api.replaceSelection(template.body.content);
  },
});


const MdLocaleSelect: React.FC<{locale: StencilApi.SiteLocale, color: string, site: StencilApi.Site, onClick: () => void}> = ({ locale, color, site, onClick }) => {
  return (
    <div style={{ fontWeight: 'bold', fontSize: 15, alignItems: 'center', color }} onClick={onClick}>
      <FormattedMessage id='pages.locale.selected' defaultMessage='selected'/> {locale?.body.value}
    </div>);
}

const getMdCommands = (locale: StencilApi.SiteLocale, color: string, site: StencilApi.Site, onClick: () => void) => {
  const localeTitle: ICommand = {
    name: locale?.body.value,
    groupName: 'title',
    keyCommand: 'title1',
    buttonProps: { },
    icon: (<MdLocaleSelect locale={locale} color={color} site={site} onClick={onClick}/>)
  };


  return [localeTitle,
    commands.group(Object.values(site.templates).map((t) => templateCommand(t)), {
      name: 'templates',
      groupName: 'templates',
      buttonProps: { 'aria-label': 'Insert Template' },
      icon: (<div style={{ fontWeight: 'bold', fontSize: 15, alignItems: 'center', color: 'blue' }}>T</div>)
    }),
    commands.group([commands.title1, commands.title2, commands.title3, commands.title4, commands.title5, commands.title6], {
      name: 'title',
      groupName: 'title',
      buttonProps: { 'aria-label': 'Insert title' },

    }),
    commands.bold,
    commands.italic,
    commands.strikethrough,
    commands.hr,
    commands.divider,
    commands.link,
    commands.quote,
    commands.code,
    commands.codeBlock,
    commands.image,
    commands.divider,
    commands.unorderedListCommand,
    commands.orderedListCommand,
    commands.checkedListCommand,
  ];
}

type PageComposerProps = {
  articleId: StencilApi.ArticleId,
  locale1: StencilApi.LocaleId,
  locale2?: StencilApi.LocaleId,
}


const ArticlePageSelect: React.FC<{ articleId: StencilApi.ArticleId, open: boolean, onClose: () => void }> = ({ open, articleId, onClose }) => {
  const { session } = Composer.useComposer();
  const view = session.getArticleView(articleId);

  return (
    <Dialog open={open}>
      <DialogTitle>
        <FormattedMessage id='pages.select.locale' defaultMessage='Select article locale'/>
      </DialogTitle>
      <DialogContent>
        <SimpleTreeView>
          {view.pages.map(pageView => (<ArticlePageItem key={pageView.page.id} article={view} page={pageView} />))}
        </SimpleTreeView>
      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={onClose}><FormattedMessage id='button.close'/></Button>
      </DialogActions>
    </Dialog>
  );
}


const ArticlePageComposer: React.FC<PageComposerProps> = ({ articleId, locale1, locale2 }) => {

  const theme = useTheme();
  const { actions, session } = Composer.useComposer();
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();
  const [localeSelect, setLocaleSelect] = React.useState(false);
  const [errors, setErrors] = React.useState(new Set<StencilApi.PageId>());

  const { site } = session;
  const view = session.getArticleView(articleId);
  const page1 = view.getPageByLocaleId(locale1).page;
  const page2 = locale2 ? view.getPageByLocaleId(locale2).page : undefined;

  const value1 = session.pages[page1.id] ? session.pages[page1.id].value : page1.body.content;
  const value2 = page2 ? (session.pages[page2.id] ? session.pages[page2.id].value : page2.body.content) : undefined;
  const articleName = session.getArticleName(articleId);

  const handleChange = (props: { page?: StencilApi.Page, value?: string }) => {
    const { page, value } = props;
    if (!page) {
      return;
    }
    actions.handlePageUpdate(page.id, value ? value : "");

    // validate
    const containsTitle = isValidTitle(value);

    // everything ok
    if (containsTitle) {
      closeSnackbar(page.id);
      const next = new Set<StencilApi.PageId>(errors);
      next.delete(page.id)
      setErrors(next);
      return;
    }

    // already reported
    if (errors.has(page.id)) {
      return;
    }

    //there is an error
    const locale = view.getPageById(page.id).locale.body.value;
    const error = <FormattedMessage id={'snack.page.missingTitle'} values={{ locale, articleName: articleName.name }} />;
    setErrors(new Set<StencilApi.PageId>(errors).add(page.id));
    enqueueSnackbar(error, { variant: 'warning', persist: true, key: page.id });

  }

  if (value2 === undefined || !page2) {
    return (<>
      <Box data-color-mode="light" sx={{ fontWeight: theme.typography.body2.fontWeight }}>
        <ArticlePageSelect articleId={articleId} open={localeSelect} onClose={() => setLocaleSelect(false)}/>

        <MDEditor 
          key={1} value={value1} 
          onChange={(value) => handleChange({ page: page1, value })} 
          
          commands={getMdCommands(session.site.locales[page1.body.locale], theme.palette.secondary.contrastText, site, () => setLocaleSelect(true))}
          textareaProps={{ placeholder: '# Title' }}
          height={800}
        />
      </Box>
    </>
    );
  }

  return (
    <Box display="flex" flexDirection="row" flexWrap="wrap">
      <ArticlePageSelect articleId={articleId} open={localeSelect} onClose={() => setLocaleSelect(false)}/>
      <Box data-color-mode="light" flex="1" sx={{ fontWeight: theme.typography.body2.fontWeight }}>
        <MDEditor key={2} value={value1} onChange={(value) => handleChange({ page: page1, value })}
          commands={getMdCommands(session.site.locales[page1.body.locale], theme.palette.secondary.contrastText, site, () => setLocaleSelect(true))}
          textareaProps={{ placeholder: '# Title' }}
          height={800}
        />

      </Box>
      <Box data-color-mode="light" flex="1" sx={{ fontWeight: theme.typography.body2.fontWeight }}>
        <MDEditor key={3} value={value2} onChange={(value) => handleChange({ page: page2, value })}
          commands={getMdCommands(session.site.locales[page2.body.locale], theme.palette.primary.light, site, () => setLocaleSelect(true))}
          textareaProps={{ placeholder: '# Title' }}
          height={800}
        />

      </Box>
    </Box>
  );
}

export { ArticlePageComposer }

