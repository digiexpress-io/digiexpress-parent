import React from 'react';


export const Toolbar: React.FC<{}> = () => {
  const drawerCtx = Burger.useDrawer();
  const drawerOpen = drawerCtx.session.drawer;
  const toggleDrawer = () => {
    drawerCtx.actions.handleDrawerOpen(!drawerOpen);
  };


  return (
    <>
    

    </>
  );
}


