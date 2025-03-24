import React from 'react';

import { ListItemText, Paper, Box, Typography, Button, Checkbox,  Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { useSnackbar } from 'notistack';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import { FormattedMessage } from 'react-intl';
import * as Burger from '@/burger';
import { StencilComposerApi as Composer } from '../stencil-setup';
import { StencilApi } from '@/burger';
import { LocaleLabels } from '../stencil-locale';
import { useFetch } from '@dxs-ts/eveli-fetch';

const selectSub = { ml: 2, color: "article.dark" }

const WorkflowComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();

  const [devMode, setDevMode] = React.useState<boolean>(false);
  const [startdate, setStartdate] = React.useState<string>('');
  const [enddate, setEnddate] = React.useState<string>('');

  let articleSelectOpen: boolean | undefined;

  const [anon, setAnon] = React.useState<boolean>(false);
  const [articleId, setArticleId] = React.useState<StencilApi.ArticleId[]>([]);
  const [technicalname, setTechnicalname] = React.useState('');
  const [labels, setLabels] = React.useState<StencilApi.LocaleLabel[]>([]);
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const locales = labels.map(l => l.locale);
  const [flowName, setFlowName] = React.useState<string>('');
  const [formName, setFormName] = React.useState<string>('');
  const [formTag, setFormTag] = React.useState<string>('');

  const { flows: allFlows = [] } = useFetch('worker/rest/api/assets/wrench/flow-names.GET', {});
  const { allTags } = useFetch('worker/rest/api/assets/dialob/tags.GET', {});


  const handleCreate = () => {
    const entity: StencilApi.CreateWorkflow = { 
      value: technicalname, 
      articles: articleId, 
      devMode, 
      labels,
      anon,
      startDate: startdate ? startdate : undefined,
      endDate: enddate ? enddate : undefined,
      flowName: flowName,
      formName: formName,
      formTag: formTag,
      formId: allTags.find(tag=> tag.formName === formName && tag.tagName === formTag)?.tagFormId,
     };
    service.create().workflow(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }
  const message = <FormattedMessage id="snack.workflow.createdMessage" />
  //const articles: StencilApi.Article[] = session.getArticlesForLocales(locales);

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

  const handleFlowNameChange = (newFlowName:string) => {
    if (technicalname === '' || technicalname === flowName) {
      setTechnicalname(newFlowName);
    }
    setFlowName(newFlowName);
  }

  const allForms = React.useMemo(() => allTags
      .filter((tag,index)=>index === allTags.findIndex(tag2=>tag2.formName === tag.formName))
      .map(tag=>{return {id:tag.formName, value:tag.formLabel}})
      .sort((a,b)=>a.value.localeCompare(b.value)), [allTags]);

    const formTags = React.useMemo(() => allTags
      .filter(t=>t.formName === formName)
      .map(tag=>{return {id:tag.tagName, value:tag.tagName}})
      .sort((a,b)=>a.value.localeCompare(b.value)), [allTags, formName]);

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='services.add' /></DialogTitle>
      <DialogContent>

        <Burger.TextField label='services.technicalname'
          required
          value={technicalname}
          onChange={setTechnicalname} />

        <Box display="flex">
          <Box flexGrow={1}>
            <Burger.Select label="services.flowName" onChange={handleFlowNameChange}
              selected={flowName}
              items={allFlows.map((flow)=>{return {id:flow, value: flow}})}
            />
          </Box>
          <Box sx={{ ml: 1 }}>
            <Burger.Select label="services.formName" onChange={setFormName}
              selected={formName}
              items={allForms}
              helperText='services.formName.description'
            />
          </Box>
          <Box sx={{ ml: 1 }}>
            <Burger.Select label="services.formTag" onChange={setFormTag}
              selected={formTag}
              items={formTags}
              helperText='services.formTag.description'
            />
          </Box>
        </Box>
        <Box display="flex">
          <Box flexGrow={1}>
            <Burger.Switch
              checked={anon ? anon : false}
              onChange={setAnon}
              helperText={"services.anonmode.helper"}
              label={"services.anonmode"}
            />
          </Box>
          <Box maxWidth="50%" sx={{ ml: 1 }}>

            <Burger.Switch
              checked={devMode}
              helperText="services.devmode.helper"
              label="services.devmode"
              onChange={setDevMode}
            />
          </Box>
        </Box>
        <Box display="flex">
          <Box flexGrow={1}>
            <Burger.DateTimeField label='services.startdate' helperText='services.startdate.description'
              required
              value={startdate}
              onChange={setStartdate} />
          </Box>
          <Box maxWidth="50%" sx={{ ml: 1 }}>
            <Burger.DateTimeField label='services.enddate' helperText='services.enddate.description'
              required
              value={enddate}
              onChange={setEnddate} />
          </Box>
        </Box>

        <LocaleLabels
          onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />

        <Paper variant="elevation" sx={{ mt: 1, pl: 1, pr: 1, pb: 1, borderRadius: 2 }}>
          <Burger.SelectMultiple label='article.select'
            multiline
            open={articleSelectOpen}
            selected={articleId}
            disabled={!locales.length}
            onChange={setArticleId}
            renderValue={(selected) => (selected as StencilApi.ArticleId[]).map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}

            items={articles.map((article) => ({
              id: article.id,
              value: (<>
                <Checkbox checked={articleId.indexOf(article.id) > -1} />
                <ListItemText primary={article.value} />
              </>)
            }))}
          />

          <Box display="flex" alignItems="center" sx={{ mt: 1, mb: 1 }}>
            <Button  onClick={() => setArticleId(Object.keys(site.articles))}  variant='text'><FormattedMessage id='allarticles'/></Button>
            <Button  onClick={() => setArticleId([])}  variant='text'><FormattedMessage id='allarticles.individual'/></Button>
            <WarningAmberRoundedIcon sx={{ ml: 3, color: "warning.main" }} /><Typography variant="caption" sx={{ ml: 1 }}><FormattedMessage id="add.allarticles.service.help" /></Typography>
          </Box>
        </Paper>
      </DialogContent>
      <DialogActions>
        <Button variant='text' onClick={onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={handleCreate} disabled={!technicalname || !flowName || !formName || !formTag || changeInProgress || labels.length < 1}>
          <FormattedMessage id='button.add'/>
        </Button>
      </DialogActions>
    </Dialog>
  );

}

export { WorkflowComposer }