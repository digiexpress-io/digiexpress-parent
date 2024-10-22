import React from 'react';
import { createBrowserRouter, createRoutesFromElements, Navigate, Route, RouterProvider } from 'react-router-dom';
import { useUserInfo } from './context/UserContext';
import { TasksView } from './views/task';
import { TaskContainer } from './views/task/TaskContainer';
import { FrontView } from './views/front/FrontView';
import { WorkflowView } from './views/workflow/WorkflowView';
import { ProcessView } from './views/process/ProcessView';
import { UnauthorizedView } from './views/front/UnauthorizedView';
import { HelpView } from './views/help/HelpView';
import { DashboardView } from './views/dashboard/DashboardView';
import { Frame, FrameProps } from './components/Frame';
import { WorkflowReleaseView } from './views/workflow/WorkflowReleaseView';
import { AssetReleaseView } from './views/release/AssetReleaseView';
import { DialobAdminView } from './views/forms/DialobAdminView';


export const AppRoutes: React.FC<FrameProps> = ({setLocale}) => {
  const baseName = import.meta.env.PUBLIC_URL;
  const userInfo = useUserInfo();
  let router = createBrowserRouter(
    createRoutesFromElements(
      <Route element={<Frame setLocale={setLocale}/>}>
        <Route path='/' element={<Navigate replace to="/ui/tasks" />}/>
        <Route path='/ui/tasks' element={<TasksView />}/>
        <Route path='/ui/forms' element={<DialobAdminView />}/>
        <Route path='/ui/tasks/task/:id' element={<TaskContainer />}/>
        <Route path='/ui/tasks/task/' element={<TaskContainer />}/>
        <Route path='/ui/workflows' element={<WorkflowView />}/>
        <Route path='/ui/workflowReleases' element={<WorkflowReleaseView />}/>
        <Route path='/ui/releases' element={<AssetReleaseView />}/>
        <Route path='/ui/processes' element={<ProcessView />}/>
        <Route path='/ui/help' element={<HelpView />}/>
        <Route path='/ui/dashboard' element={<DashboardView />}/>
      </Route>
    ),
    {
      basename: baseName
    }
  );
  if (!userInfo.isAuthenticated()) { // Public user routes
    router = createBrowserRouter(
      createRoutesFromElements(
        <Route element={<Frame setLocale={setLocale}/>}>
          <Route path='/*' element={<FrontView />}/>
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
        <Route element={<Frame setLocale={setLocale}/>}>
          <Route path='/*' element={<UnauthorizedView />}/>
        </Route>
    ),
    {
      basename: baseName
    }
    );
  }
  return (
    <RouterProvider router={router} />
  );
}
