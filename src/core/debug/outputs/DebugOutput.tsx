import React from 'react';

import { Client } from '../../context';
import { DebugOutputCsv } from './DebugOutputCsv';
import { DebugOutputsDt } from './DebugOutputsDt';
import { DebugOutputsFl } from './DebugOutputsFl';
import { DebugOutputsFt } from './DebugOutputsFt';


const DebugOutput: React.FC<{
  selected?: Client.AstBody;
  debug?: Client.DebugResponse;
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
    delegate = (<DebugOutputsDt debug={debug.body as Client.DecisionResult}/>);
  } else if (bodyType === "FLOW_TASK") {
    delegate = (<DebugOutputsFt debug={debug.body as Client.ServiceResult} />);
  } else if (bodyType === "FLOW") {
    delegate = (<DebugOutputsFl debug={debug.body as Client.FlowResult}/>);
  }
  return delegate;
}

export type { };
export { DebugOutput };
