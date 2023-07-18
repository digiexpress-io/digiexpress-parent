import ModelAPI from './ModelAPI';


declare namespace AstAPI {

  // Decision Table
  interface Dt {
    name: string;
    description?: string;
    rev: number;
    hitPolicy: "FIRST" | "ALL";
    headerTypes: ModelAPI.ValueType[];
    headerExpressions: Record<ModelAPI.ValueType, string[]>;
    headers: DtHeader[];
    rows: DtRow[];
  }
  interface DtHeader  { id: string, order: number, name: string, value: DtHeaderValue, direction: ModelAPI.ModelDirection, script?: string }
  interface DtRow     { id: string, order: number, cells: DtCell[] }
  interface DtCell    { id: string, header: string, value?: string }
  interface DtCommand { id: string; value?: string; type: DtCommandType }

  type DtHeaderValue = "STRING" | "BOOLEAN" | "INTEGER" | "LONG" | "DECIMAL" | "DATE" | "DATE_TIME";
  type DtCommandType =
    "SET_NAME" | "SET_DESCRIPTION" | "IMPORT_ORDERED_CSV"|
    "MOVE_ROW" | "MOVE_HEADER" | "INSERT_ROW" | "COPY_ROW" |
    
    "SET_HEADER_TYPE" | "SET_HEADER_REF" | "SET_HEADER_SCRIPT" | "SET_HEADER_DIRECTION" | "SET_HEADER_EXPRESSION" | 
    "SET_HIT_POLICY" | "SET_CELL_VALUE" |
    
    "DELETE_CELL" | "DELETE_HEADER" | "DELETE_ROW" |
    "ADD_LOG" | "ADD_HEADER_IN" | "ADD_HEADER_OUT" | "ADD_ROW";
    

  // Flow
  interface Fl {
    name: string;
    description?: string;
    src: FlBody;
    commands: FlCommand[];
    messages: FlCommandMessage[];
    autocomplete: FlAutocomplete[];
  }
  
  interface FlCommand         { id: number; value: string; type: "SET" | "ADD" | "DELETE"; }
  interface FlAutocomplete    { id: string; value: string[]; range: FlCommandRange[]; }
  interface FlCommandMessage  { line: number; value: string; type: "ERROR" | "WARNING"; range?: FlCommandRange; }
  interface FlCommandRange    { start: number; end: number; column?: number; insert?: boolean; }
  interface FlInputType { name: string; ref: string; value: string; }
  
  interface FlNode {
    parent: FlNode;
    keyword: string;
    children: Record<string, FlNode>;
    value: string;
    start: number; 
    end: number;
  }
  
  interface FlBody extends FlNode {
    id: FlNode;
    description: Node;
    types: FlInputType[];
    inputs: Record<string, FlInput>;
    tasks: Record<string, FlTask>;
  }
  
  interface FlInput extends FlNode {
    required: FlNode;
    type: FlNode;
    debugValue: FlNode;
  }
  
  interface FlTask extends FlNode {
    id: FlNode;
    then: FlNode;
    order: number;
    
    ref?: FlRef;
    decisionTable?: FlRef;
    service?: FlRef;
    switch?: Record<string, FlSwitch>;
  }

  interface FlRef extends FlNode {
    ref: FlNode;
    collection: FlNode;
    inputsNode: FlNode;
    inputs: Record<string, FlNode>;
  }

  interface FlSwitch extends FlNode {
    order: number;
    when: FlNode;
    then: FlNode;
  }


  // Flow task
  interface Ft {
    name: string;
    src: string;
    rev: number;
    commands: FtCommand[];
    method: FtMethod;
  }

  interface FtCommand { id: number; value: string; type: "SET" | "ADD" | "DELETE"; }
  interface FtMethod {
    id: string;
    name: string;
    returnType: boolean;
    parameters: FtParameter[];
  }
  
  interface FtParameter {
    type: ModelAPI.DataType;
    contextType: "INTERNAL" | "EXTERNAL";
  }
  

  // Parser
  interface Parser {
    type: ModelAPI.ServiceType;
    service: Dt | Fl | Ft;
  }

  interface ParserCommands {
    input: ParserCommand[];
    rev: number;
    type: ModelAPI.ServiceType;
  }
  interface ParserCommand {
    id?: number | string;
    value?: string;
    type: string;
  }

  interface Service {
    getParser(commands: ParserCommands): Promise<Parser>;
  }

}

export default AstAPI;