import React from 'react';

import { ListItemText, Paper, Box, Typography, Button, Checkbox, Dialog, DialogTitle, DialogContent, DialogActions, useTheme, Divider } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage } from 'react-intl';

import { StencilApi, StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import * as Burger from '@dxs-ts/eveli-primitives';
import { LocaleLabels } from '../stencil-locale';
import { useFetch } from '@dxs-ts/envir-fetch';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { WorkflowConfigOptions, WorkflowOptions } from './WorkflowConfigOptions';



interface WorkflowEditProps {
  workflowId: StencilApi.WorkflowId,
  onClose: () => void,
}

const WorkflowEdit: React.FC<WorkflowEditProps> = ({ onClose, workflowId }) => {
  const { enqueueSnackbar } = useSnackbar();
  const theme = useTheme();
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
    .filter((tag, index) => index === allDialobTags.findIndex(tag2 => tag2.formName === tag.formName))
    .map(({ formName, formLabel }) => ({ id: formName, value: formLabel }))
    .filter(tag => !!tag.value)
    .sort((a, b) => a.value.localeCompare(b.value)), [allDialobTags]);

  const formTags = React.useMemo(() => allDialobTags
    .filter((tag) => tag.formName === formName)
    .map(({ tagName }) => ({ id: tagName, value: tagName }))
    .filter(tag => !!tag.value)
    .sort((a, b) => a.value.localeCompare(b.value)), [allDialobTags, formName]);

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='services.edit' /></DialogTitle>
      <DialogContent>

        <LocaleLabels
          onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />


        <Burger.TextField label='services.technicalname'
          required
          value={technicalname}
          onChange={setTechnicalname} />

        <Box display="flex">
          <Box>
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
          <Box sx={{ ml: 1 }} flexGrow={1}>
            <Burger.Select label="services.flowName" onChange={setFlowName}
              selected={flowName}
              items={allFlows.map((flow) => ({ id: flow, value: flow }))}
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

        <Divider sx={{ my: theme.spacing(2) }} />
          <Typography fontWeight='bold'><FormattedMessage id='composer.select.article' /></Typography>
          <Burger.SelectMultiple label='composer.article.selected'
            variant='ARTICLE_SELECT'
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

        <WorkflowConfigOptions onChange={handleOptionsChange} value={workflowOptions} />
        <Box mb={theme.spacing(3)} />

      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!technicalname || !flowName || !formName || !formTag || changeInProgress || labels.length < 1}>
          <FormattedMessage id='button.apply' />
        </Button>
      </DialogActions>
    </Dialog>
  );

}

export { WorkflowEdit }
