import React from 'react'
import { IamApi } from './iam-types';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { IamForcedLogin } from './IamForcedLogin';



export const IamLiveness: React.FC<{
  onExpire: () => void;
  timeout?: number | undefined;
  user: IamApi.User | undefined;
}> = (props) => {

  const { getLiveness } = useFetch('worker/rest/api/iam/liveness.GET', {})
  const { timeout = 60000, onExpire, user } = props;
  const [timeLeft, setTimeLeft] = React.useState<number>();

  

  // start liveness after login
  React.useEffect(() => {
    if(!user) {
      return;
    }
    const timer = setTimeout(async () => {
      const v = await getLiveness();
      const expiresIn = v ? v.expiresIn * 1000 : -1;
      setTimeLeft(expiresIn);

      if (expiresIn <= 1000) {
        onExpire();
      }
    }, timeout);
    return () => clearTimeout(timer);
  }, [timeLeft, setTimeLeft, onExpire, user]);

  return (<></>);
}