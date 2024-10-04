import React from 'react';

import { Container, Toolbar } from '@mui/material';

import {
  GShell,
  GFooter,
  GShellClassName,
  GForm,
  GLogo,
  GLayout,
  useIam
} from '../';
import { useNavigate } from '@tanstack/react-router';


export const RouterOffer: React.FC<{ 
  offerId: string;
  formId: string;
  productId: string;

 }> = (props) => {

   const nav = useNavigate();
   const anon = useIam();
   const anonymousUser = anon.authType === 'ANON';

   function onAfterComplete() {
     if (anonymousUser) {
       // TODO::: dont know where 
       alert('TODO::: route somewhere');
     }
     else {
       nav({
         from: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId',
         to: '/secured/$locale/pages/$pageId/products/$productId/offers/$offerId/summary',
       })
     }
   }


  return (
    <GShell drawerOpen={false}>
      <Toolbar className={GShellClassName}>
        <GLayout variant='toolbar-n-rows-2-columns'>
          <GLogo variant='black_lg' />
        </GLayout>
      </Toolbar>

      <main role='main'>
        <Container>
          <GForm variant={props.productId} onAfterComplete={onAfterComplete}>{props.formId}</GForm>
        </Container>
      </main>

      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell>

  );
}