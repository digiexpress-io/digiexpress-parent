import React from 'react';

import { HdesApi } from '@/burger';
import { DebugOutputCsv } from './DebugOutputCsv';
import { DebugOutputsDt } from './DebugOutputsDt';
import { DebugOutputsFl } from './DebugOutputsFl';
import { DebugOutputsFt } from './DebugOutputsFt';


const DebugOutput: React.FC<{
  selected?: HdesApi.AstBody;
  debug?: HdesApi.DebugResponse;
}> = ({ selected, debug }) => {

  if(!selected || !debug) {
    return null;
  }

  const bodyType = selected?.bodyType;
  console.log("Debug asset", debug);

  let delegate = (<></>);
  if(!debug.body) {
    if (debug.bodyCsv) {
      delegate = <DebugOutputCsv debug={debug.bodyCsv} />;
    }
  } else if (bodyType === "DT") {
    delegate = (<DebugOutputsDt debug={debug.body as HdesApi.DecisionResult}/>);
  } else if (bodyType === "FLOW_TASK") {
    delegate = (<DebugOutputsFt debug={debug.body as HdesApi.ServiceResult} />);
  } else if (bodyType === "FLOW") {
    delegate = (<DebugOutputsFl debug={debug.body as HdesApi.FlowResult}/>);
  }
  return delegate;
}

export type { };
export { DebugOutput };
