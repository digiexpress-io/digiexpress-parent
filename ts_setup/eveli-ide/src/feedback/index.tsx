
import React from 'react';
import { SnackbarProvider } from 'notistack';
import { createBrowserRouter, createRoutesFromElements, Navigate, Route, RouterProvider, Outlet, useParams, useMatch, Routes } from 'react-router-dom';

import { Composer } from '../stencil/context';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';
import { Secondary } from './Secondary';
import { Toolbar } from './Toolbar';


const locale = 'en';
const composer: BurgerApi.App<Composer.ContextType> = {
  id: "feedback-composer",
  components: { primary: Outlet, secondary: Secondary, toolbar: Toolbar },
  state: [
    (children: React.ReactNode, restorePoint?: BurgerApi.AppState<Composer.ContextType>) => (<>{children}</>),
    () => ({})
  ]
}

const StartComposer: React.FC<{}> = () => {
  return (<Burger.Provider children={[composer]} drawerOpen />)
}


interface FeedbackComposerProps {

}

export const FeedbackComposer: React.FC<FeedbackComposerProps> = () => {
  //const { taskId } = useParams<{ taskId: string }>();
  return (<Routes>
    <Route element={<StartComposer />}>
      <Route path='/feedback/:taskId' element={<>real feedback for task</>} />
      <Route path='/feedback' element={<>feedback loading...</>} />
    </Route>
  </Routes>)
}

