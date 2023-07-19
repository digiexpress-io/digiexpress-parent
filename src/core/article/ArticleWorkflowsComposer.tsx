import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import Burger from '@the-wrench-io/react-burger';

import { Composer, StencilClient } from '../context';

/*
const comparator = (o1: StencilClient.Workflow, o2: StencilClient.Workflow) => ((o1.body.devMode ? "a-" : "b-") + o1.body.value)
  .localeCompare(((o2.body.devMode ? "a-" : "b-") + o2.body.value));
*/

const ArticleWorkflowsComposer: React.FC<{ articleId: StencilClient.ArticleId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site, session } = Composer.useComposer();
  const layout = Burger.useTabs();

  const view = session.getArticleView(props.articleId);
  const workflows: StencilClient.Workflow[] = Object.values(site.workflows)
    .map((w) => ({ w, name: session.getWorkflowName(w.id)?.name }))
    .sort((a, b) => a.name.localeCompare(b.name))
    .map((w) => w.w);

  const handleSave = (selectedWorkflows: string[]) => {
    const article = site.articles[props.articleId]
    const entity: StencilClient.ArticleMutator = {
      articleId: article.id,
      name: article.body.name,
      parentId: article.body.parentId,
      order: article.body.order,
      workflows: [...selectedWorkflows],
      links: undefined,
      devMode: article.body.devMode
    };
    console.log("saving selected services" + selectedWorkflows);
    service.update().article(entity)
      .then(_success => actions.handleLoadSite())
      .then(() => layout.actions.handleTabCloseCurrent())
    enqueueSnackbar(message, { variant: 'success' });
  }

  const message = <FormattedMessage id="snack.workflow.editedMessage" />
  const articleName = site.articles[props.articleId].body.name;


  return (
    <>
      <Burger.TransferList
        title="articleservices"
        titleArgs={{ name: articleName }}
        searchTitle="services.technicalname"
        selectedTitle="services.selected"
        headers={["services.technicalname", "services.devmode"]}
        rows={workflows.map(row => row.id)}
        filterRow={(row, search) => {
          const workflow = site.workflows[row];
          return workflow.body.value.toLowerCase().indexOf(search) > -1;
        }}
        renderCells={(row) => [session.getWorkflowName(row).name, site.workflows[row].body.devMode ? "DEV" : ""]}
        selected={view.workflows.map(l => l.workflow.id)}
        cancel={{
          label: 'button.cancel',
          onClick: () => layout.actions.handleTabCloseCurrent()
        }}
        submit={{
          label: "button.apply",
          disabled: false,
          onClick: handleSave
        }}
      />
    </>
  );
}

export { ArticleWorkflowsComposer }

