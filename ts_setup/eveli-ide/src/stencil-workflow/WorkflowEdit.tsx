import React from 'react';

import { ListItemText, Paper, Box, Typography, Button, Checkbox, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { useSnackbar } from 'notistack';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

import { FormattedMessage } from 'react-intl';

import { StencilComposerApi as Composer } from '@/stencil-setup';
import { StencilApi } from '@/api-stencil';
import * as Burger from '@/eveli-styles';
import { LocaleLabels } from '../stencil-locale';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { CancelButton } from '@/eveli-styles';
import { WorkflowConfigOptions, WorkflowOptions } from './WorkflowConfigOptions';



interface WorkflowEditProps {
  workflowId: StencilApi.WorkflowId,
  onClose: () => void,
}

const WorkflowEdit: React.FC<WorkflowEditProps> = ({ onClose, workflowId }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const workflow = site.workflows[workflowId];

  const [startdate, setStartdate] = React.useState<string>(workflow.body.startDate ?? '');
  const [enddate, setEnddate] = React.useState<string>(workflow.body.endDate ?? '');

  const [articleId, setArticleId] = React.useState<StencilApi.ArticleId[]>(workflow.body.articles);
  const [technicalname, setTechnicalname] = React.useState(workflow.body.value);
  const [labels, setLabels] = React.useState<StencilApi.LocaleLabel[]>(workflow.body.labels);
  const [flowName, setFlowName] = React.useState<string>(workflow.body.flowName || '');
  const [formName, setFormName] = React.useState<string>(workflow.body.formName || '');
  const [formTag, setFormTag] = React.useState<string>(workflow.body.formTag || '');
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const [workflowOptions, setWorkflowOptions] = React.useState<WorkflowOptions>({
    anon: workflow.body.anon,
    devMode: workflow.body.devMode,
    disabled: workflow.body.disabled
  });

  const { flows: allFlows = [] } = useFetch('worker/rest/api/assets/wrench/flow-names.GET', {});
  const { allTags: allDialobTags } = useFetch('worker/rest/api/assets/dialob/tags.GET', {});


  const handleCreate = () => {

    const entity: StencilApi.WorkflowMutator = { 
      workflowId: workflow.id, 
      value: technicalname, 
      articles: articleId, 
      devMode: workflowOptions.devMode,
      anon: workflowOptions.anon,
      disabled: workflowOptions.disabled,
      labels,
      startDate: startdate ? startdate : undefined,
      endDate: enddate ? enddate : undefined,
      flowName: flowName,
      formName: formName,
      formTag: formTag,
      formId: allDialobTags.find(tag => tag.formName === formName && tag.tagName === formTag)?.tagFormId,
    };


    service.update().workflow(entity).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    })
  }


  function handleOptionsChange(newOptions: WorkflowOptions) {
    setWorkflowOptions(newOptions);
  }

  const message = <FormattedMessage id="snack.workflow.editedMessage" />
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
    }));


    const allForms = React.useMemo(() => allDialobTags
      .filter((tag,index) => index === allDialobTags.findIndex(tag2=>tag2.formName === tag.formName))
      .map(({formName, formLabel}) => ({id: formName, value: formLabel}))
      .sort((a,b) => a.value.localeCompare(b.value)), [allDialobTags]);
  
    const formTags = React.useMemo(() => allDialobTags
      .filter((tag) => tag.formName === formName)
      .map(({tagName}) => ({id: tagName, value: tagName}))
      .sort((a,b)=> a.value.localeCompare(b.value)), [allDialobTags, formName]);

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='services.edit' /></DialogTitle>
      <DialogContent>
        <Burger.TextField label='services.technicalname'
          required
          value={technicalname}
          onChange={setTechnicalname} />

        <Box display="flex">
          <Box flexGrow={1}>
            <Burger.Select label="services.flowName" onChange={setFlowName}
              selected={flowName}
              items={allFlows.map((flow)=>({id:flow, value: flow}))}
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


        <WorkflowConfigOptions onChange={handleOptionsChange} value={workflowOptions} />



        <LocaleLabels
          onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />

        <Paper variant="elevation" sx={{ mt: 1, pl: 1, pr: 1, pb: 1, borderRadius: 2 }}>
          <Burger.SelectMultiple label='composer.select.article'
            multiline
            onChange={setArticleId}
            selected={articleId}
            renderValue={(selected) => (selected as StencilApi.ArticleId[]).map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}

            items={articles.map((article) => ({
              id: article.id,
              value: (<>
                <Checkbox checked={articleId.indexOf(article.id) > -1} />
                <ListItemText primary={article.value} />
              </>
              )
            }))}
          />
          <Box display="flex" alignItems="center" sx={{ mt: 1, mb: 1 }}>
            <Button  onClick={() => setArticleId(Object.keys(site.articles))}  variant='text'><FormattedMessage id='allarticles'/></Button>
            <Button  onClick={() => setArticleId([])}  variant='text'><FormattedMessage id='allarticles.individual'/></Button>
            <WarningAmberRoundedIcon sx={{ ml: 3, color: "warning.main" }} /><Typography variant="caption" sx={{ ml: 1 }}>
              <FormattedMessage id="add.allarticles.service.help" />
            </Typography>
          </Box>
        </Paper>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!technicalname || !flowName || !formName || !formTag || changeInProgress || labels.length < 1 }>
          <FormattedMessage id='button.apply'/>
        </Button>
      </DialogActions>
    </Dialog>
  );

}

export { WorkflowEdit }