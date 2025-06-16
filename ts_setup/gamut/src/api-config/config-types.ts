import { createIamFetch } from './fetch_iam';
import { createSiteFetch } from './fetch_stencil';
import { createDialobFetch, createPublicDialobFetch } from './fetch_dialob';
import { createOfferFetch, createPublicOfferFetch } from './fetch_offer';
import { createAuthFeedbackFetch, createContractFetch, createSubjectFetch } from './fetch_portal';
import { createBookingFetch } from './fetch_booking';

import { version, build_time } from '../version'


const logo = `
 ______ _______ _______ _     _ _______
|  ____ |_____| |  |  | |     |    |   
|_____| |     | |  |  | |_____|    |   
version - ${version}
build time - ${build_time}
`;


type RecursivePartial<T> = {
    [P in keyof T]?: RecursivePartial<T[P]>;
};

export namespace ConfigApi {

  // defaults
  export const default_config = {
    iamFetch: createIamFetch(),
    siteFetch: createSiteFetch(),
    dialobFetch: createDialobFetch(),
    dialobPublicFetch: createPublicDialobFetch(),
    offerFetch: createOfferFetch(),
    publicOfferFetch: createPublicOfferFetch(),
    contractFetch: createContractFetch(),
    subjectFetch: createSubjectFetch(),
    bookingFetch: createBookingFetch(),
    authFeedbackFetch: createAuthFeedbackFetch(),

    iamLiveness: 60000,
    staleTime: 5 * 1000,
    processesQueryKey: 'legacy-processes',
    handleExpire: () => alert('sessionExpired')
  }

  export function mergeOptions(init: Options): ConfigContextType {
    const next = { ...default_config }

    console.groupCollapsed('Gamut config');
    console.log(`%c ${logo}`, "color:#A020F0; font-size:10px; font-weight:900;")


    for(const [key, value] of Object.entries(init)) {
      if(!value) {
        continue;
      }

      if((typeof value) === 'function') {
        console.log('overriding function', key, value);
        //@ts-ignore
        next[key] = value;
        continue;
      }

      if((typeof value) !== 'object') {
        console.log('overriding plain value', key, value);
        //@ts-ignore
        next[key] = value;
        continue;
      }

      for(const [restApiKey, restApiMethod] of Object.entries(value)) {
        if(!restApiMethod) {
          continue;
        }
        console.log('overriding rest api', key, restApiKey, restApiMethod);
        //@ts-ignore
        (next[key] as any)[restApiKey] = restApiMethod;
      }
    }
    console.log('Merged config', next)
    console.groupEnd();

    return next;
  }
}

export declare namespace ConfigApi {

  export type ConfigContextType = typeof default_config & {

  }

  export type Options = RecursivePartial<typeof default_config>;
}