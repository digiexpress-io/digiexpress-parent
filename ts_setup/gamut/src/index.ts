import { version, build_time } from './version'

export * from './g-shell'
export * from './g-logo'

export * from './g-popover-button'
export * from './g-popover-search'
export * from './g-popover-topics'

export * from './g-confirm'

export * from './g-article-feedback';
export * from './g-article-feedback-viewer';

export * from './g-props'
export * from './g-app-bar'
export * from './g-divider'

export * from './g-locales'
export * from './g-login'

export * from './g-md'
export * from './g-layout'

export * from './g-bookings'

export * from './g-inbox'
export * from './g-inbox-messages'
export * from './g-inbox-attachments'
export * from './g-inbox-form-review'

export * from './g-loader'

export * from './g-confirm'

export * from './g-sort';

export * from './g-date'

export * from './g-tooltip'

export * from './g-article'

export * from './g-link'
export * from './g-links'
export * from './g-links-page'

export * from './g-form-review';
export * from './g-form'
export * from './g-form-base'
export * from './g-user-overview-menu'
export * from './g-user-overview'

export * from './g-secured-services'
export * from './g-secured-services-search'

export * from './g-footer'

export * from './g-flex'

export * from './g-offers'
export * from './g-contracts'
export * from './g-bookings'

export * from './api-dialob'
export * from './api-site'
export * from './api-comms'
export * from './api-iam'
export * from './api-offer'
export * from './api-bookings'
export * from './api-contract'
export * from './api-locale'
export * from './api-search'

export * from './router'


export * from './g-router-bookings'
export * from './g-router-forms-awaiting-decision'
export * from './g-router-forms-with-decision'
export * from './g-router-inbox'
export * from './g-router-inbox-subject'
export * from './g-router-offer'
export * from './g-router-offer-summary'
export * from './g-router-product'
export * from './g-router-secured-services'
export * from './g-router-unfinished-forms'
export * from './g-router-unsecured'
export * from './g-router-user-overview'
export * from './g-override'


const logo = `
 ______ _______ _______ _     _ _______
|  ____ |_____| |  |  | |     |    |   
|_____| |     | |  |  | |_____|    |   
version - ${version}
build time - ${build_time}
`;

console.log(`%c ${logo}`, "color:#A020F0; font-size:10px; font-weight:900;")
