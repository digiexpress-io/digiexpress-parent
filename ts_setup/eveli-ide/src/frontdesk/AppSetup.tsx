import React from 'react';
import { IntlProvider } from 'react-intl'
import { createBrowserRouter, createRoutesFromElements, Navigate, Route, RouterProvider, Outlet, useMatch } from 'react-router-dom';
import { useUserInfo } from './context/UserContext';
import { FrontView } from './views/front/FrontView';
import { UnauthorizedView } from './views/front/UnauthorizedView';
import { TasksView } from './views/task';
import { TaskContainer } from './views/task/TaskContainer';
import { WorkflowView } from './views/workflow/WorkflowView';
import { ProcessView } from './views/process/ProcessView';
import { HelpView } from './views/help/HelpView';
import { DashboardView } from './views/dashboard/DashboardView';
import { WorkflowReleaseView } from './views/workflow/WorkflowReleaseView';
import { PublicationsView } from './views/publications/PublicationsView';
import { DialobAdminView } from './views/forms/DialobAdminView';

import { useConfig } from './context/ConfigContext';
import { TaskSessionContext } from './context/TaskSessionContext';

import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';
import { StencilComposer, StencilClient } from '../stencil';
import { WrenchComposer, WrenchClient } from '../wrench';
import { Secondary } from './Secondary';
import { Toolbar } from './Toolbar';

import { frontdeskIntl } from './intl'
import { stencilIntl } from '../stencil'
import { wrenchIntl } from '../wrench'
import { FeedbackProvider } from './context/FeedbackContext';


const StartRouter: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const basename = import.meta.env.PUBLIC_URL;
  return <RouterProvider router={createBrowserRouter(createRoutesFromElements(children), { basename })} />
}

const frontdeskApp: BurgerApi.App<{}> = {
  id: "frontdesk-app",
  components: { primary: Outlet, secondary: Secondary, toolbar: Toolbar },
  state: [
    (children: React.ReactNode, restorePoint?: BurgerApi.AppState<{}>) => (
      <>{children}</>),
    () => ({})
  ]
}

const StartFrame: React.FC<{ locale: string }> = ({ locale }) => {
  const isWrench = useMatch({ path: '/wrench/ide' })
  const isStencil = useMatch({ path: '/ui/content' })
  const { serviceUrl } = useConfig();

  if (isWrench) {
    const service = new WrenchClient.ServiceImpl(new WrenchClient.DefaultStore({ url: serviceUrl + "rest/api/assets/wrench" }));
    return (
      <IntlProvider locale='en' messages={wrenchIntl.en}>
        <WrenchComposer service={service} />
      </IntlProvider>)
  } else if (isStencil) {
    const service = StencilClient.service({ config: { url: serviceUrl + "rest/api/assets/stencil" } });
    return (
      <IntlProvider locale='en' messages={stencilIntl.en}>
        <StencilComposer service={service} />
      </IntlProvider>)
  }

  return (
    <FeedbackProvider>
      <IntlProvider locale={locale} messages={frontdeskIntl[locale]}>
        <TaskSessionContext>
          <Burger.Provider children={[frontdeskApp]} drawerOpen />
        </TaskSessionContext>
      </IntlProvider>
    </FeedbackProvider>

  );
}

export const AppSetup: React.FC<{ locale: string }> = ({ locale }) => {
  const userInfo = useUserInfo();

  if (!userInfo.isAuthenticated()) { // Public user routes
    return (
      <StartRouter>
        <Route element={<StartFrame locale={locale} />}>
          <Route path='/*' element={<FrontView />} />
        </Route>
      </StartRouter>);

  } else if (!userInfo.isAuthorized()) {
    return (
      <StartRouter>
        <Route element={<StartFrame locale={locale} />}>
          <Route path='/*' element={<UnauthorizedView />} />
        </Route>
      </StartRouter>);
  }

  return (<StartRouter>
    <Route element={<StartFrame locale={locale} />}>
      <Route path='/' element={<Navigate replace to="/ui/tasks" />} />
      <Route path='/ui/tasks' element={<TasksView />} />
      <Route path='/ui/forms' element={<DialobAdminView />} />
      <Route path='/ui/tasks/task/:id' element={<TaskContainer />} />
      <Route path='/ui/tasks/task/' element={<TaskContainer />} />
      <Route path='/ui/workflows' element={<WorkflowView />} />
      <Route path='/ui/workflowReleases' element={<WorkflowReleaseView />} />
      <Route path='/ui/publications' element={<PublicationsView />} />
      <Route path='/ui/processes' element={<ProcessView />} />
      <Route path='/ui/help' element={<HelpView />} />
      <Route path='/ui/dashboard' element={<DashboardView />} />

      <Route path='/wrench/ide' element={<>wrench loading...</>} />
      <Route path='/ui/content' element={<>stencil loading...</>} />
    </Route>
  </StartRouter>)
};
