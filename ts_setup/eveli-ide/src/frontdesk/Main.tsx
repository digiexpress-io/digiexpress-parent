import React from 'react';
import { Box } from '@mui/system';

import { createBrowserRouter, createRoutesFromElements, Navigate, Outlet, Route, RouterProvider } from 'react-router-dom';
import { TasksView } from './views/task';
import { DialobAdminView } from './views/forms/DialobAdminView';
import { TaskContainer } from './views/task/TaskContainer';
import { WorkflowView } from './views/workflow/WorkflowView';
import { WorkflowReleaseView } from './views/workflow/WorkflowReleaseView';
import { AssetReleaseView } from './views/release/AssetReleaseView';
import { ProcessView } from './views/process/ProcessView';
import { HelpView } from './views/help/HelpView';
import { DashboardView } from './views/dashboard/DashboardView';
import { FrontView } from './views/front/FrontView';
import { UnauthorizedView } from './views/front/UnauthorizedView';
import { Feedback } from './components/Feedback';
import { useUserInfo } from './context/UserContext';


const Frame: React.FC<{ setLocale: (locale: string) => void }> = ({ setLocale }) => {
  return (
    <>
      <Outlet />
      <Feedback />
    </>
  )
}

const root = { height: `100%`, padding: 1, backgroundColor: "mainContent.main" };

export const Main: React.FC = () => {
  const baseName = import.meta.env.PUBLIC_URL;
  const userInfo = useUserInfo();
  let router = createBrowserRouter(
    createRoutesFromElements(
      <Route element={<Frame setLocale={() => { }} />}>
        <Route path='/' element={<Navigate replace to="/ui/tasks" />} />
        <Route path='/ui/tasks' element={<TasksView />} />
        <Route path='/ui/forms' element={<DialobAdminView />} />
        <Route path='/ui/tasks/task/:id' element={<TaskContainer />} />
        <Route path='/ui/tasks/task/' element={<TaskContainer />} />
        <Route path='/ui/workflows' element={<WorkflowView />} />
        <Route path='/ui/workflowReleases' element={<WorkflowReleaseView />} />
        <Route path='/ui/releases' element={<AssetReleaseView />} />
        <Route path='/ui/processes' element={<ProcessView />} />
        <Route path='/ui/help' element={<HelpView />} />
        <Route path='/ui/dashboard' element={<DashboardView />} />
      </Route>
    ),
    {
      basename: baseName
    }
  );
  if (!userInfo.isAuthenticated()) { // Public user routes
    router = createBrowserRouter(
      createRoutesFromElements(
        <Route element={<Frame setLocale={() => { }} />}>
          <Route path='/*' element={<FrontView />} />
        </Route>
      ),
      {
        basename: baseName
      }
    );
  }
  else if (!userInfo.isAuthorized()) {
    router = createBrowserRouter(
      createRoutesFromElements(
        <Route element={<Frame setLocale={() => { }} />}>
          <Route path='/*' element={<UnauthorizedView />} />
        </Route>
      ),
      {
        basename: baseName
      }
    );
  }

  return (<RouterProvider router={router} />)
}
