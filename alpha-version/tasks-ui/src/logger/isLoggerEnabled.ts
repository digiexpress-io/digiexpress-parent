import { LogLevel, Logger } from './logger-types';

const LEVELS: Record<LogLevel, number> = {
  "OFF": 0,
  "ERROR": 200,
  "WARN": 300,
  "INFO": 400,
  "DEBUG": 500,
  "TRACE": 600,
}

export function isLoggerEnabled(level: LogLevel, logger: Logger): boolean {

  const config = window.LOGGER?.config ?? logger.config;

  const given = LEVELS[level];
  const loggerName = logger.name;
  for(const [pkg, defined] of Object.entries(config.values)) {
    const filterFound = loggerName.startsWith(pkg) || loggerName.match(pkg);
    if(!filterFound) {
      continue;
    }

    const filterLevel = LEVELS[defined];
    return given <= filterLevel;
  }
  const defined = LEVELS[config.level];
  return given <= defined;
}