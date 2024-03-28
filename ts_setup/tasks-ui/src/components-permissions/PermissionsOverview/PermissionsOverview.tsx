import React from 'react';
import { Permission, ImmutablePermissionStore } from 'descriptor-permissions';
import Context from 'context';


const PermissionsOverview: React.FC = () => {
  const backend = Context.useBackend();
  const [permissions, setPermissions] = React.useState<Permission[]>();

  React.useEffect(() => {
    new ImmutablePermissionStore(backend.store).findPermissions().then(setPermissions);
  }, []);


  return (<>{permissions?.map((p) => <span key={p.id}>{p.name}</span>)}</>)
}

export { PermissionsOverview };

