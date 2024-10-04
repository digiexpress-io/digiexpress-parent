import React from 'react'
import { IamApi } from './iam-types';


export const IamLiveness: React.FC<{
  fetchUserLivenessGET: IamApi.FetchUserLivenessGET;
  timeout: number | undefined;
  onExpire: () => void;
  user: IamApi.User | undefined;
}> = (props) => {

  const { timeout = 60000, fetchUserLivenessGET, onExpire, user } = props;
  const [timeLeft, setTimeLeft] = React.useState<number>();

  // start liveness after login
  React.useEffect(() => {
    if(!user) {
      return;
    }

    const timer = setTimeout(async () => {
      const resp = await fetchUserLivenessGET();
      const v: { expiresIn: number } | undefined = resp.ok ? await resp.json() : undefined;

      const expiresIn = v ? v.expiresIn * 1000 : -1;
      setTimeLeft(expiresIn);


      if (expiresIn <= 1000) {
        onExpire();
      }
    }, timeout);
    return () => clearTimeout(timer);
  }, [timeLeft, setTimeLeft, fetchUserLivenessGET, onExpire, user]);

  return (<></>);
}