import { Hdes } from '../deps';

declare namespace ExplorerAPI {
  type FilterType = "FL" | "ST" | "DT" | "TS";
  type FilterDef = { id: string, value: FilterType, serviceType?: Hdes.ModelAPI.ServiceType };
  type Filters = Record<FilterType, FilterDef>;
}


const filters: ExplorerAPI.Filters = {
  "FL": {id: "package.explorer.flows",           value: "FL", serviceType: "FLOW"},
  "ST": {id: "package.explorer.servicetasks",    value: "ST", serviceType: "FLOW_TASK"},
  "DT": {id: "package.explorer.decisiontables",  value: "DT", serviceType: "DT"},
  "TS": {id: "package.explorer.timestamps",      value: "TS"},
};

export {filters};
export default ExplorerAPI;