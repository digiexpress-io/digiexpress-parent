import React from 'react';

import { ListItemText, Paper, Box, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

import { FormattedMessage } from 'react-intl';

import { Composer, StencilApi } from '../context';
import * as Burger from '@/burger';
import { LocaleLabels } from '../locale';



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

  const [devMode, setDevMode] = React.useState(workflow.body.devMode);
  const [articleId, setArticleId] = React.useState<StencilApi.ArticleId[]>(workflow.body.articles);
  const [technicalname, setTechnicalname] = React.useState(workflow.body.value);
  //const articles: StencilApi.Article[] = session.getArticlesForLocales(workflow.body.labels.map(l => l.locale));
  const [labels, setLabels] = React.useState<StencilApi.LocaleLabel[]>(workflow.body.labels);
  const [changeInProgress, setChangeInProgress] = React.useState(false);

  const handleCreate = () => {
    var offset = new Date().getTimezoneOffset();
    console.log(offset);
    const entity: StencilApi.WorkflowMutator = { 
      workflowId: workflow.id, 
      value: technicalname, 
      articles: articleId, 
      labels, devMode,
      startDate: startdate ? startdate + ":00" : undefined,
      endDate: enddate ? enddate + ":00": undefined,
    };
    service.update().workflow(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
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

  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="services.edit"
      submit={{ title: "button.apply", onClick: handleCreate, disabled: !technicalname || changeInProgress || labels.length < 1 }}>

      <>
        <LocaleLabels
          onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />
        <Paper variant="elevation" sx={{ mt: 1, pl: 1, pr: 1, pb: 1, borderRadius: 2 }}>

          <Box display="flex">
            <Box flexGrow={1}>
              <Burger.TextField label='services.technicalname' helperText='services.technicalname.description'
                required
                value={technicalname}
                onChange={setTechnicalname} />
            </Box>
            <Box maxWidth="50%" sx={{ ml: 1 }}>
              <Burger.Switch
                checked={devMode ? devMode : false}
                onChange={setDevMode}
                helperText={"services.devmode.helper"}
                label={"services.devmode"}
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


          <Burger.SelectMultiple label='composer.select.article'
            multiline
            onChange={setArticleId}
            selected={articleId}
            renderValue={(selected) => (selected as StencilApi.ArticleId[]).map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}

            items={articles.map((article) => ({
              id: article.id,
              value: (<>
                <Burger.Checkbox checked={articleId.indexOf(article.id) > -1} />
                <ListItemText primary={article.value} />
              </>
              )
            }))}
          />
          <Box display="flex" alignItems="center" sx={{ mt: 1, mb: 1 }}>
            <Burger.SecondaryButton label={"allarticles"} onClick={() => setArticleId(Object.keys(site.articles))} />
            <Burger.SecondaryButton label={"allarticles.individual"} onClick={() => setArticleId([])} />
            <WarningAmberRoundedIcon sx={{ ml: 3, color: "warning.main" }} /><Typography variant="caption" sx={{ ml: 1 }}>
              <FormattedMessage id="add.allarticles.service.help" />
            </Typography>
          </Box>
        </Paper>
      </>
    </Burger.Dialog >
  );

}

export { WorkflowEdit }