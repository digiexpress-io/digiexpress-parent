import messages from './intl';
export { siteTheme } from './burger';
export type { BurgerApi } from './burger';
export type { StencilApi} from './stencil';
export type { HdesApi } from './wrench';

export { StencilComposer, StencilClient } from './stencil';
export { WrenchComposer, WrenchClient } from './wrench';
export { Frontdesk } from './frontdesk';

export { FeedbackComposer } from './feedback';



export const feedbackIntl: Record<string, any> = messages;
export const wrenchIntl: Record<string, any> = messages;
export const stencilIntl: Record<string, any> = messages;
export const frontdeskIntl: Record<string, any> = messages;