import ModelAPI from './ModelAPI';
import AstAPI from './AstAPI';


declare namespace DebugAPI {

  interface FlDebug {
    result: {
      // key - value
      _errors?: { msg: string, stackTrace: string }
    }
    debug: FlExecution;
  }

  interface FlExecution {
    id: string;
    model: AstAPI.Fl;
    context: FlContext;
    log: Record<string, Object>;
  }

  interface FlContext {
    status: FlStatus;
    pointer: string;
    history: FlHistory[];
    shortHistory: string;
    tasks: FlTask[];
    variables: Record<string, Object>;
  }

  type FlTaskStatus = "OPEN" | "COMPLETED";
  type FlStatus = "CREATED" | "RUNNING" | "SUSPENDED" | "ENDED"

  interface FlHistory {
    id: string;
    modelId: string;
    start: string;
    end: string;
  }
  interface FlTask {
    id: string;
    modelId: string;
    status: FlTaskStatus;
    variables: Record<string, any>;
    inputs: Record<string, any>;
  }


  interface DtDebug {
    rejections: DtDecision[];
    matches: DtDecision[];
    outputs: DtOutput[];
  }

  interface DtDecision {
    match: boolean;
    context: DtContext[];
    expressions: Record<string, DtExpression>;
    node: {
      order: number,
      inputs: [{
        key: { name: string },
        value: string
      }
      ]
    }
  }

  interface DtContext {
    key: ModelAPI.DataType;
    value: Object;
  }

  interface DtOutput {
    id: number;
    order: number;
    expressions: Record<string, DtExpression>;
    values: Record<string, any>;
  }

  interface DtExpression {
    src: string;
    type: ModelAPI.ValueType;
    constants: string[];
  }

  type FtDebug = Record<string, any>

  interface Debug {
    type: ModelAPI.ServiceType;
    service: FlDebug | DtDebug | FtDebug
  }

  interface DebugCommands {
    id: string; content?: string;
    input?: string; inputCsv?: string;
    type: ModelAPI.ServiceType;
  }

  interface Service {
    getDebug(props: DebugCommands): Promise<Debug>;
  }
}

export default DebugAPI;
