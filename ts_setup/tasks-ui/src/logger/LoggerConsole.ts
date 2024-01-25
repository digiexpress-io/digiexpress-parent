
import { LogObjectId, Logger, Args, LoggerConfig, LoggingMessage, LogLevel } from './logger-types';
import { isLoggerEnabled } from './isLoggerEnabled';
import { log } from 'console';

export function createDefaultConfig(): LoggerConfig {
  return {
    format: 'STRING',
    level: 'TRACE'
  };
}

export function createContext(logger: LoggerConsole): string {
  let result = '';
  if(logger.payload?.code) {
    result += "/codes/" + logger.payload.code;
  }
  if(logger.payload?.userId) {
    result += "/user_id/" + logger.payload.code;
  }
  if(logger.payload?.id) {
    result += "/object_id/" + JSON.stringify(logger.payload.id);
  }
  return result;
}

export function mergePayload(start: Payload | undefined, next: Payload): Payload {
  if(!start) {
    return { ...next };
  }

  return { ...start, ...next }
}

export function createArgs(logger: LoggerConsole): Args {
  const start = logger.payload;
  if(!start) {
    return { };
  }

  const extra: Args = {};

  if(start.code) {
    extra['code'] = start.code;
  }
  if(start.userId) {
    extra['userId'] = start.userId;
  }
  if(start.id) {
    extra['id'] = JSON.stringify(start.id);
  }
  return { ...(start.args ?? {}), ...extra };
}


export interface Payload {
  id?: LogObjectId;
  code?: string;
  userId?: string;
  args?: Args;
  objects?: object[];
}

export class LoggerConsole implements Logger {
  private _loggerName: string;
  private _config: LoggerConfig;
  private _payload: Payload | undefined;

  constructor(loggerName: string, config: LoggerConfig, payload?: Payload) {
    this._config = config;
    this._loggerName = loggerName;
    this._payload = payload;
  }

  trace (message: string, error?: Error) { this.withMessage('TRACE',  message, error) }
  info  (message: string, error?: Error) { this.withMessage('INFO',   message, error) }
  warn  (message: string, error?: Error) { this.withMessage('WARN',   message, error) }
  error (message: string, error?: Error) { this.withMessage('ERROR',  message, error) }  
  debug (message: string, error?: Error) { this.withMessage('DEBUG',  message, error) }

  get config() { return this._config }
  get name() { return this._loggerName }
  get payload() { return this._payload }

  id(target: LogObjectId) { return this.clone({ id: target })}
  code(uniqueMessageCode: string) { return this.clone({ code: uniqueMessageCode }) }
  userId(userId: string) { return this.clone({ userId }) }
  args(args: Args) { return this.clone({ args }) }
  objects(objects: object[]) { return this.clone({ objects }) }
  object(object: object) { return this.clone({ objects: [object] }) }

  clone (payload: Payload): Logger {
    return new LoggerConsole(this._loggerName, this._config, mergePayload(this._payload, payload))
  }

  withMessage(level: LogLevel, message: string, error?: Error) {
    const enabled = isLoggerEnabled(level, this);
    if(!enabled) {
      return;
    }

    const logger = this._loggerName;
    const args: Args = createArgs(this);
    const context = createContext(this);
    const msg: LoggingMessage = {
      logger, level, message, error, args, context
    }

    if(level === 'ERROR') {
      console.error(msg.logger, msg.context, msg.message, this._payload?.objects);
    } else {
      console.log(msg.logger, msg.context, msg.message, this._payload?.objects);
    }
  }
}
