import Session from './Session';

enum ActionType {
  addTab = "addTab", 
  removeTab = "removeTab", 
  changeTab = "changeTab",
  closeTabs = "closeTabs",
  
  setDrawer = "setDrawer",
  setTabData = "setTabData", 
  setDialog = "setDialog", 
  setLink = "setLink", 
  setSearch = "setSearch", 
  setDimensions = "setDimensions",
}

interface Action {
  type: ActionType;
  
  setDimensions?: {width: number, height: number};
  setDrawer?: boolean;
  setDialog?: string;
  setLink?: string;
  setSearch?: { keyword?: string, tab?: Session.Tab<any> };
  addTab?: Session.Tab<any>;
  removeTab?: string;
  changeTab?: number;
  setTabData?: {id: string, updateCommand: (oldData: any) => any};
}

const ActionBuilder = {
  setDimensions: (setDimensions: {width: number, height: number}) => ({ type: ActionType.setDimensions, setDimensions }),
  setDrawer: (setDrawer: boolean) => ({ type: ActionType.setDrawer, setDrawer }),
  addTab: (addTab: Session.Tab<any>): Action => ({ type: ActionType.addTab, addTab }),
  removeTab: (removeTab: string): Action => ({ type: ActionType.removeTab, removeTab}),
  changeTab: (changeTab: number): Action => ({ type: ActionType.changeTab, changeTab}),
  closeTabs: (): Action => ({ type: ActionType.closeTabs }),
  
  setTabData: (id: string, updateCommand: (oldData: any) => any): Action => ({
    type: ActionType.setTabData, 
    setTabData: {id, updateCommand}
  }),
 
  setLink: (setLink?: string): Action => ({ type: ActionType.setLink, setLink}),
  setDialog: (setDialog?: string): Action => ({ type: ActionType.setDialog, setDialog}),
  setSearch: (keyword: string, tab?: Session.Tab<any>): Action => ({ type: ActionType.setSearch, setSearch: { keyword, tab }}),
}

class ReducerDispatch implements Session.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  constructor(session: React.Dispatch<Action>) {
    console.log("init layout actions");
    this._sessionDispatch = session;
  }
  handleDrawer(open?: true) {
    this._sessionDispatch(ActionBuilder.setDrawer(open === true)) 
  }
  handleDimensions(props: {width: number, height: number}) {
    this._sessionDispatch(ActionBuilder.setDimensions(props)) 
  }  
  handleSearch(keyword: string) {
    this._sessionDispatch(ActionBuilder.setSearch(keyword)) 
  }  
  handleLink(id: string) {
    this._sessionDispatch(ActionBuilder.setLink(id)) 
  } 
  handleDialog(id: string) {
    this._sessionDispatch(ActionBuilder.setDialog(id)) 
  }
  handleTabAdd(newItem: Session.Tab<any>) {
    this._sessionDispatch(ActionBuilder.addTab(newItem)); 
  }
  handleTabChange(tabIndex: number) {
    this._sessionDispatch(ActionBuilder.changeTab(tabIndex))
  }
  handleTabClose(tab: Session.Tab<any>) {
    this._sessionDispatch(ActionBuilder.removeTab(tab.id));
  }
  handleTabCloseAll() {
    this._sessionDispatch(ActionBuilder.closeTabs());
  }
}

const Reducer = (state: Session.InstanceMutator, action: Action): Session.InstanceMutator => {
  switch (action.type) {
    case ActionType.addTab: {
      if(action.addTab) {
        return state.withTab(action.addTab); 
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.changeTab: {
      if(action.changeTab === undefined) {
        console.error("Action data error", action);
        return state;
      }
      return state.withTab(action.changeTab);
    }
    case ActionType.removeTab: {
      if(!action.removeTab) {
        console.error("Action data error", action);
        return state;
      }
      return state.deleteTab(action.removeTab);      
    }
    case ActionType.setTabData: {
      if(!action.setTabData) {
        console.error("Action data error", action);
        return state;
      }
      return state.withTabData(action.setTabData.id, action.setTabData.updateCommand);
    }
    case ActionType.setSearch: {
      const search = action.setSearch;
      if(!search) {
        console.error("Action data error", action);
        return state;
      }
      
      const newState = state.withSearch(search.keyword ? search.keyword : "")
      if(search.tab) {
        return newState.withTab(search.tab);        
      }
      return newState;
    }
    case ActionType.setDialog: {
      return state.withDialog(action.setDialog)
    }
    case ActionType.setLink: {
      return state.withLink(action.setLink)
    }
    case ActionType.closeTabs: {
      return state.deleteTabs();
    }
    case ActionType.setDrawer: {
      const open = action.setDrawer === true;
      if(state.drawer.open === open) {
        return state;
      }
      
      return state.withDrawer({open});
    }
    case ActionType.setDimensions: {
      const dim = action.setDimensions;
      if(!dim) {
        console.error("Action data error", action);
        return state; 
      }
      
      let width = dim.width;
      const height = dim.height - 64;
      console.log("handle dimensions width x height: " + width + "x" + height);
      if(width !== state.dimensions.width || height !== state.dimensions.height) {
        return state.withDimensions({width, height});  
      }
      return state;
    }
  }
}
export type { Action }
export { Reducer, ReducerDispatch, ActionType };
