import {confirmationMiddleware} from './confirmationMiddleware';
import {backendMiddleware} from './backendMiddleware';
import {previewMiddleware} from './previewMiddleware';
import {downloadMiddleware} from './downloadMiddleware';
import {extensionMiddleware} from './extensionMiddleware';
import {navigationMiddleware} from './navigationMiddleware';
import {createScrollMiddleware} from '@resys/react-redux-scroll';

const middleware = [
  navigationMiddleware,
  confirmationMiddleware,
  extensionMiddleware,
  backendMiddleware,
  previewMiddleware,
  downloadMiddleware,
  createScrollMiddleware()
];

export default function createDialobComposerMiddleware() {
  return middleware;
}
