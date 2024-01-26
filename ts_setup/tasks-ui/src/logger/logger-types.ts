
export type InfoLogger = (message: string, optionalParams?: any) => void;
export type WarnLogger = (message: string, optionalParams?: any) => void;
export type ErrorLogger = (message: string, optionalParams?: any) => void;
export type TraceLogger = (message: string, optionalParams?: any) => void;
export type DebugLogger = (message: string, optionalParams?: any) => void;

export type LogLevel = 'OFF' | 'TRACE' | 'DEBUG' | 'INFO' | 'ERROR' | 'WARN';

export type TargetObject = object;
export type PkgWithSrc = string;


export interface LoggingMessage {
  level: LogLevel;
  logger: string; 
  message: string;
  code: string | undefined;
  target: TargetObject | undefined;
  args: any;
}

export interface LoggerConfig {
  format: 'STRING'
  level: LogLevel;
  values: Record<PkgWithSrc, LogLevel>
}


export interface Logger {
  name: string;
  config: LoggerConfig;

  code: (uniqueMessageCode: string) => Logger;
  target: (target: TargetObject) => Logger

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