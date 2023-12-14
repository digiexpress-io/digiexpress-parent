import {default as ExplorerAs} from './Explorer';
import {Tab as TabAs, openTab as openTabAs} from './Tab';


namespace Explorer {
  export const openTab = openTabAs;
  export const Tab = TabAs;
  export const View = ExplorerAs;
  
}

export default Explorer;