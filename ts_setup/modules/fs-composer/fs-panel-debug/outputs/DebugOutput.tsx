import React from 'react';
import { Fs } from '@dxs-ts/fs-api';
import { DebugOutputCsv } from './DebugOutputCsv';
import { DebugOutputsDt } from './DebugOutputsDt';
import { DebugOutputsFl } from './DebugOutputsFl';
import { DebugOutputsFt } from './DebugOutputsFt';


const DebugOutput: React.FC<{
  selected?: Fs.DirentBase;
  debug?: Fs.DebugResponse;
}> = ({ selected, debug }) => {

  if (!selected || !debug) {
    return null;
  }

  const bodyType = selected?.type;

  let delegate = (<></>);
  if (!debug.body) {
    if (debug.bodyCsv) {
      delegate = <DebugOutputCsv debug={debug.bodyCsv} />;
    }
  } else if (bodyType === "DECISION_TABLE") {
    delegate = (<DebugOutputsDt debug={debug.body as Fs.DecisionResult}/>);
  } else if (bodyType === "FLOW_TASK") {
    delegate = (<DebugOutputsFt debug={debug.body as Fs.ServiceResult} />);
  } else if (bodyType === "FLOW") {
    delegate = (<DebugOutputsFl debug={debug.body as Fs.FlowResult}/>);
  }
  return delegate;
}

export type { };
export { DebugOutput };
