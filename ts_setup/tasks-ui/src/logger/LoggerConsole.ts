
import { Logger, LoggerConfig, LoggingMessage, LogLevel } from './logger-types';
import { isLoggerEnabled } from './isLoggerEnabled';

export function createDefaultConfig(): LoggerConfig {
  return { format: 'STRING', level: 'TRACE', values: {} };
}

export interface Payload { code?: string; target?: any; }


export class LoggerConsole implements Logger {
  private _loggerName: string;
  private _config: LoggerConfig;
  private _payload: Payload | undefined;

  constructor(loggerName: string, config: LoggerConfig, payload?: Payload) {
    this._config = config;
    this._loggerName = loggerName;
    this._payload = payload;
  }

  get config() { return this._config }
  get name() { return this._loggerName }
  get payload() { return this._payload }
  
  trace (message: string, optionalParams?: any[]) { this.withMessage('TRACE',  message, optionalParams) }
  info  (message: string, optionalParams?: any[]) { this.withMessage('INFO',   message, optionalParams) }
  warn  (message: string, optionalParams?: any[]) { this.withMessage('WARN',   message, optionalParams) }
  error (message: string, optionalParams?: any[]) { this.withMessage('ERROR',  message, optionalParams) }  
  debug (message: string, optionalParams?: any[]) { this.withMessage('DEBUG',  message, optionalParams) }

  code(code: string) { return this.clone({ code }) }
  target(target: any) { return this.clone({ target })}
  clone (payload: Payload): Logger {
    return new LoggerConsole(this._loggerName, this._config, { ...this._payload, ...payload});
  }

  writeMessage(msg: LoggingMessage) {  }

  getColor(level: LogLevel): string {
    if(level === 'DEBUG') {
      return "color:green; font-size:10px;";
    } else if(level === 'ERROR') {
      return "color:#FF595E; font-size:15px;";
    }
    return "";
  }

  withMessage(level: LogLevel, message: string, args: any[] | undefined) {
    const enabled = isLoggerEnabled(level, this);
    if(!enabled) {
      return;
    }

    const color = this.getColor(level);

    const logger = this._loggerName;
    const code = this._payload?.code;
    const suffix = code ? `code: ${code}` : `logger: ${logger}`;
    const target = this._payload?.target;
    const groupName = `%c${message} - ${suffix}`;

    const msg: LoggingMessage = { logger, level, message, code, target, args: args ?? [] };
    this.writeMessage(msg);

    if(args) {
      console.groupCollapsed(groupName, color, args);
      console.log(`message: ${message}`,args);
    } else {
      console.groupCollapsed(groupName, color);
      console.log(`message: ${message}`);
    }

    if(args) {
      console.log("args", args) 
    }
    if(target) {
      console.log("target", target);
    }

    console.trace("TRACE");
    console.groupEnd();
  }
}
