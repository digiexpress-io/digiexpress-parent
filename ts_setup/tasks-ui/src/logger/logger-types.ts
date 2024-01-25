
export type InfoLogger = (message: string, error?: Error) => void;
export type WarnLogger = (message: string, error?: Error) => void;
export type ErrorLogger = (message: string, error?: Error) => void;
export type TraceLogger = (message: string, error?: Error) => void;
export type DebugLogger = (message: string, error?: Error) => void;

export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'ERROR' | 'WARN';
export type LogObjectId = string | number | object;
export type Args = Record<string, string|number|boolean|Date|undefined>;

export interface LoggingMessage {
  level: LogLevel;
  logger: string; 
  context: string;
  message: string;
  error?: Error;
  args: Args;
}

export interface LoggerConfig {
  format: 'JSON' | 'STRING'
  level: LogLevel;
}


export interface Logger {
  name: string;
  config: LoggerConfig;

  id: (target: LogObjectId) => Logger;
  code: (uniqueMessageCode: string) => Logger;
  userId: (userId: string) => Logger;
  args: (args: Args) => Logger;

  objects: (obj: object[]) => Logger;
  object: (obj: object) => Logger;

  trace: TraceLogger;
  info: InfoLogger;
  warn: WarnLogger;
  debug: DebugLogger;
  error: ErrorLogger;
}

export interface LoggerFactory {
  getLogger: (id?: string) => Logger;
}

export interface LoggerProps {
  impl?: LoggerFactory; // override logger
  config?: LoggerConfig;
}

declare global {
  interface Window { LOGGER?: LoggerProps; }
}