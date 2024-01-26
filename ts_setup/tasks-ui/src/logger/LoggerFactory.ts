
import { Logger, LoggerFactory, LoggerProps, LoggerConfig } from './logger-types';
import { createDefaultConfig, LoggerConsole } from './LoggerConsole';

export class LoggerFactoryDelegate implements LoggerFactory {
  private _delegate: LoggerFactory;
  private _config: LoggerConfig;

  constructor() {
    if(window.LOGGER && window.LOGGER.config) {
      this._config = window.LOGGER.config;
    } else {
      this._config = createDefaultConfig();
    }

    if(window.LOGGER && window.LOGGER.impl) {
      this._delegate = window.LOGGER.impl;
    } else {
      this._delegate = new LoggerFactoryConsole(this._config);
    }
  }

  getLogger(id?: string) {
    return this._delegate.getLogger(id);
  }
}


export class LoggerFactoryConsole implements LoggerFactory {
  private _config: LoggerConfig;
  constructor(config: LoggerConfig) {
    this._config = config;
  }

  getLogger(id?: string): Logger {
    let loggerName: string;
    if (typeof id === 'string') {
      loggerName = id as string;
    } else {
      try {
        const meta = new Error().stack;
        const stack = meta?.replace("\r\n", "\n").split("\n");
        if(stack) {
          const fileNameStack = stack[stack?.length -1];
          const dirtyFilename = fileNameStack.substring(fileNameStack.indexOf("src"));

          loggerName = dirtyFilename.substring(0, dirtyFilename.indexOf("?"));
        } else {
          loggerName = "";
        }
      } catch(Error) {
        loggerName = "";
      }
    }
    const logger: Logger = new LoggerConsole(loggerName, this._config);
    return logger;
  }
}
