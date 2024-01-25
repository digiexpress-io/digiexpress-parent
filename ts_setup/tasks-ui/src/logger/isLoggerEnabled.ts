import { LogLevel, Logger } from './logger-types';

export function isLoggerEnabled(level: LogLevel, logger: Logger): boolean {
  return true;
}