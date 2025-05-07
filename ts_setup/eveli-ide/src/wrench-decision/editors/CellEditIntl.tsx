import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, } from '@mui/material';
import { HdesApi } from '@/api-wrench';

import MDEditor, { ICommand, commands } from '@uiw/react-md-editor';
import IntlBuilder from './builders/TypeIntlBuilder';
import { CancelButton } from '@/eveli-styles';



const MdLocaleSelect: React.FC<{ locale: string }> = ({ locale }) => {
  return (
    <div style={{ fontWeight: 'bold', fontSize: 15, alignItems: 'center' }}>
      {locale}
    </div>);
}

const getMdCommands = (
  dt: HdesApi.AstDecision,
  header: HdesApi.TypeDef,
) => {
  const localeTitle: ICommand = {
    name: header.name,
    groupName: 'title',
    keyCommand: 'title1',
    buttonProps: {},
    icon: (<MdLocaleSelect locale={header.name} />)
  };

  return [
    localeTitle,
    commands.group([commands.title1, commands.title2, commands.title3, commands.title4, commands.title5, commands.title6], {
      name: 'title',
      groupName: 'title',
      buttonProps: { 'aria-label': 'Insert title' },

    }),
    commands.bold,
    commands.italic,
    commands.strikethrough,
    commands.hr,
    commands.divider,
    commands.link,
    commands.quote,
    commands.code,
    commands.codeBlock,
    commands.image,
    commands.divider,
    commands.unorderedListCommand,
    commands.orderedListCommand,
    commands.checkedListCommand,
  ];
}



interface CellEditIntlProps {
  dt: HdesApi.AstDecision,
  cell: HdesApi.AstDecisionCell;
  locale: string;
  onClose: () => void;
  onChange: (commands: HdesApi.AstCommand[]) => void
};

const CellEditIntl: React.FC<CellEditIntlProps> = (props) => {

  const header: HdesApi.TypeDef = [...props.dt.headers.acceptDefs, ...props.dt.headers.returnDefs]
    .filter(t => t.id === props.cell.header)[0];
  const [value, setValue] = React.useState(new IntlBuilder({ header, value: props.cell.value ?? '' }));


  const handleChangeValue = (value: string) => {
    setValue(prev => prev.withLocale(props.locale, value));
  }

  return (<Dialog open={true} onClose={props.onClose}>
    <DialogTitle>
      <FormattedMessage id='decisions.cells.dialog.title' values={{
        name: props.dt.name,
        column: header.name,
        value: props.locale
      }} />

    </DialogTitle>
    <DialogContent>
      <MDEditor
        commands={getMdCommands(props.dt, header)}
        textareaProps={{ placeholder: '# Title' }}
        height={800}
        value={value.getLocaleValue(props.locale)}
        onChange={(value: any) => handleChangeValue(value ?? '')}

      />;
    </DialogContent>
    <DialogActions>
      <Button variant='text' onClick={() => {
        setValue(prev => prev.withLocale(props.locale, ''));
      }}>
        <FormattedMessage id="decisions.cells.newvalue.clear" />
      </Button>
      <CancelButton onClick={props.onClose} />
      <Button onClick={() => {
        const command: HdesApi.AstCommand = { id: props.cell.id, value: value.value, type: 'SET_CELL_VALUE' };
        props.onChange([command]);
        props.onClose();
      }}>
        <FormattedMessage id='buttons.apply' />
      </Button>
    </DialogActions>
  </Dialog>
  );
}

export type { CellEditIntlProps };
export { CellEditIntl };
