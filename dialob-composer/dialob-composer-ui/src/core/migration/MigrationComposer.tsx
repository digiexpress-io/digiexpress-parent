import React from 'react';

import Burger from '@the-wrench-io/react-burger';
import { Composer } from '../context';

const MigrationComposer: React.FC<{ onClose: () => void}> = ({onClose}) => {
  const [file, setFile] = React.useState<string | undefined>();
  const [loading, setLoading] = React.useState<boolean | undefined>();
  const { service, actions } = Composer.useComposer();

  const handleCreate = () => {
    if (!file) {
      return;
    }
    setLoading(true);
    service.create().importData(file)
      .then(() => actions.handleLoadSite())
      .then(() => {
        setLoading(false);
        setFile(undefined);
        onClose();
      });
  }

  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main" 
      title="migrations.title"
      submit={{ title: "migrations.create", onClick: handleCreate, disabled: loading || !file }}>
      <>
        <Burger.FileField value="" onChange={setFile} label="migrations.select" />
      </>
    </Burger.Dialog>


  );
}

export { MigrationComposer };
