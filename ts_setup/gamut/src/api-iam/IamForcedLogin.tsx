import { IamApi } from './iam-types'




var __login_window: Window | null = null;

function openLoginWindow(props: IamForcedLoginProps): boolean {
  if (__login_window == null) {
    const left = window.screenX + 30;
    const top = window.screenY + 30;
    __login_window = window.open(props.loginUrl, "_blank", `height=600,width=400,left=${left},top=${top}`);
    return true;
  }
  return false;
}

function awaitTillLoggedIn(props: IamForcedLoginProps): Promise<void> {
  return new Promise<void>((resolve, reject) => {
    
    // timeout in case login is required but not logged in
    setTimeout(() => reject(), 60000);

    console.log("iam forced login")
    const loop = () => {
      props.user().then((response) => {



        if (response.status === 401 || response.status === 403) {
          if (__login_window != null && !__login_window.closed) {
            setTimeout(loop, 1000);
          } else {
            __login_window = null;
            reject();
          }
        } else {
          __login_window?.close();
          __login_window = null;
          resolve();
        }
      });
    }


    if (__login_window != null && !__login_window.closed) {
      setTimeout(loop, 1000);
    } else {
      resolve();
    }
  });
}


export interface IamForcedLoginProps {
  loginUrl: string;
  user: IamApi.FetchUserGET
}

export function IamForcedLogin(props: IamForcedLoginProps): Promise<void> {
  console.log(props);
  openLoginWindow(props);
  return awaitTillLoggedIn(props);
}