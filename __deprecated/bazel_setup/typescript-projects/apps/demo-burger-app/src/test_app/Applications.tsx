import React from 'react';
import Burger from "@digiexpress/react-burger";
import Toolbar from './Toolbar';


const DemoBurger: React.FC<{}> = () => {
  const [app1State] = React.useState<App1ContextType>({});

  const app1: Burger.App<App1ContextType> = {
    id: "app-1",
    components: { primary: Primary1, secondary: Secondary1, toolbar: Toolbar1 },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<App1ContextType>) => {
        console.log(restorePoint);
        return <App1Provider>{children}</App1Provider>;
      },
      () => app1State
    ]
  };
  const app2: Burger.App<App2ContextType> = {
    id: "app-2",
    components: { primary: Primary2, secondary: Secondary2, toolbar: Toolbar2 },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<App2ContextType>) => {
        console.log(restorePoint);
        return <App2Provider>{children}</App2Provider>;
      },
      () => app1State
    ]
  }
    ;
  //const app3: Burger.App = {};
  /*  
      state: [ AppStateCreate<T>, AppStateRestore<T> ] 
    components: {
      toolbar: React.ElementType<ToolbarProps>;
      primary: React.ElementType<PrimaryProps>;
      secondary: React.ElementType<SecondaryProps>;
    }
    */

  return (

    <Burger.Provider children={[
      app1,
      app2, 
      //app3
    ]} />);
}

const Primary1: React.FC<{}> = () => {
  const { actions } = Burger.useApps();
  return (<div>
    primary app1
    <Burger.PrimaryButton label="to-app-2" onClick={() => actions.handleActive("app-2")} />
  </div>);
}
const Secondary1: React.FC<{}> = () => {
  return (<div>secondary app1</div>);
}
const Toolbar1: React.FC<{}> = () => {
  return (<Toolbar>#1</Toolbar>);
}

const Primary2: React.FC<{}> = () => {
  const { actions } = Burger.useApps();
  return (<div>primary app2
    <Burger.PrimaryButton label="to-app-1" onClick={() => actions.handleActive("app-1")} />
  </div>);
}
const Secondary2: React.FC<{}> = () => {
  return (<div>secondary app2</div>);
}
const Toolbar2: React.FC<{}> = () => {
  return (<Toolbar>#2</Toolbar>);
}


interface App1ContextType { }
const App1Context = React.createContext<App1ContextType>({});
const App1Provider: React.FC<{children: React.ReactNode}> = (props) => {
  return (<App1Context.Provider value={{}}>{props.children}</App1Context.Provider>);
}


interface App2ContextType { }
const App2Context = React.createContext<App2ContextType>({});
const App2Provider: React.FC<{children: React.ReactNode}> = (props) => {
  return (<App2Context.Provider value={{}}>{props.children}</App2Context.Provider>);
}

export default DemoBurger;

