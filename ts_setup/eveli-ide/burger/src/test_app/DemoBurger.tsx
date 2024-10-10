import React from 'react';
import Burger from "../core"
import BurgerStyles from "../core/styles"

import { Box } from '@mui/material';



const DemoBurger: React.FC<{}> = () => {


  const [app1State, setApp1State] = React.useState<App1ContextType>({});

  const app1: Burger.App<App1ContextType> = {
    id: "app-1",
    components: { primary: Primary1, secondary: Secondary1, toolbar: Toolbar1 },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<App1ContextType>) => <App1Provider>{children}</App1Provider>,
      () => app1State
    ]
  };
  const app2: Burger.App<App2ContextType> = {
    id: "app-2",
    components: { primary: Primary2, secondary: Secondary2, toolbar: Toolbar2 },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<App2ContextType>) => <App2Provider>{children}</App2Provider>,
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

  const [text, setText] = React.useState<string>()
  const [number, setNumber] = React.useState<number>()
  const [date, setDate] = React.useState<string>()
  const [dateTime, setDateTime] = React.useState<string>()

  return (
    <Box sx={{ m: 5, display: 'flex', flexDirection: 'row', justifyContent: 'space-evenly' }}>
      <Box sx={{ width: 0.4 }}>
        <BurgerStyles.TextField
          label="TextField"
          value={text}
          placeholder='Placeholder'
          onChange={setText}
        />
        <BurgerStyles.TextField
          label="TextField"
          value={text}
          placeholder='Placeholder'
          onChange={setText}
          helperText='Helper text'
          errorMessage='Error message'
          error={true}
        />
        <BurgerStyles.TextField
          label="TextField"
          value={text}
          placeholder='Placeholder'
          onChange={setText}
          helperText='Helper text'
          errorMessage='Error message'
          error={false}
        />

        <BurgerStyles.NumberField
          label="NumberField"
          value={number}
          placeholder='Enter a number'
          onChange={setNumber}
        />
        <BurgerStyles.NumberField
          label="NumberField"
          value={number}
          placeholder='7'
          onChange={setNumber}
          helperText='Helper text'
          errorMessage='Error message'
          error={true}
        />
        <BurgerStyles.NumberField
          label="NumberField"
          value={number}
          placeholder='5'
          onChange={setNumber}
          helperText='Helper text'
          errorMessage='Error message'
          error={false}
        />

        <BurgerStyles.DateField
          label="DateField"
          value={date}
          placeholder='Placeholder'
          onChange={setDate}
        />
        <BurgerStyles.DateField
          label="DateField"
          value={date}
          placeholder='Placeholder'
          onChange={setDate}
          helperText='Helper text'
          errorMessage='Error message'
          error={true}
        />
        <BurgerStyles.DateField
          label="DateField"
          value={date}
          placeholder='Placeholder'
          onChange={setDate}
          helperText='Helper text'
          errorMessage='Error message'
          error={false}
        />
      </Box>
      <Box sx={{ width: 0.4 }}>
        <BurgerStyles.DateTimeField
          label="DateTimeField"
          value={dateTime}
          placeholder='Placeholder'
          onChange={setDateTime}
        />
        <BurgerStyles.DateTimeField
          label="DateTimeField"
          value={dateTime}
          placeholder='Placeholder'
          onChange={setDateTime}
          helperText='Helper text'
          errorMessage='Error message'
          error={true}
        />
        <BurgerStyles.DateTimeField
          label="DateTimeField"
          value={dateTime}
          placeholder='Placeholder'
          onChange={setDateTime}
          helperText='Helper text'
          errorMessage='Error message'
          error={false}
        />

        <BurgerStyles.SearchField
          label="SearchField"
          value={text}
          placeholder='Placeholder'
          onChange={setText}
        />
        <BurgerStyles.SearchField
          label="SearchField"
          value={text}
          placeholder='Placeholder'
          onChange={setText}
          helperText='Helper text'
          errorMessage='Error message'
          error={true}
        />
        <BurgerStyles.SearchField
          label="SearchField"
          value={text}
          placeholder='Placeholder'
          onChange={setText}
          helperText='Helper text'
          errorMessage='Error message'
          error={false}
        />
      </Box>
    </Box>
  );
}
const Secondary1: React.FC<{}> = () => {
  return (<div>secondary app1</div>);
}
const Toolbar1: React.FC<{}> = () => {
  return (<div>toolbar app1</div>);
}

const Primary2: React.FC<{}> = () => {
  const { actions } = Burger.useApps();
  return (<div>primary app2
    <BurgerStyles.PrimaryButton label="to-app-1" onClick={() => actions.handleActive("app-1")} />
  </div>);
}
const Secondary2: React.FC<{}> = () => {
  return (<div>secondary app2</div>);
}
const Toolbar2: React.FC<{}> = () => {
  return (<div>toolbar app2</div>);
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

