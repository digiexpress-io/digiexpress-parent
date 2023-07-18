import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Toolbar, Typography, Button, ButtonGroup, Tooltip } from '@material-ui/core';
import EditIcon from '@material-ui/icons/Edit';
import DoubleArrowRoundedIcon from '@material-ui/icons/DoubleArrowRounded';
import CompareArrowsRoundedIcon from '@material-ui/icons/CompareArrowsRounded';
import FileDownloadDoneIcon from '@material-ui/icons/FileDownloadDone';
import UploadIcon from '@material-ui/icons/Upload';

import { FormattedMessage } from 'react-intl';
import fileDownload from 'js-file-download'


import { Hdes } from './deps';
import MetaEditor from './edit/MetaEditor';
import UploadCSV from './edit/UploadCSV';
import RowsColumns from './edit/RowsColumns';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      height: '64px',
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(1),
    },
    title: {
      flex: '1 1 100%',
      color: theme.palette.text.primary,
    },
  }),
);

interface DecisionTableToolbarProps {
  dt: Hdes.AstAPI.Dt;
  model: Hdes.ModelAPI.Model;
  onChange: (commands: Hdes.AstAPI.DtCommand[]) => void;
}

const DecisionTableToolbar: React.FC<DecisionTableToolbarProps> = ({ dt, onChange, model }) => {
  const classes = useStyles();
  const [edit, setEdit] = React.useState<"META" | "ROWS-COLUMNS" | "CSV_UPLOAD">();

  const saveCsv = () => {
    //const columns = this.props.header;
    const headers = dt.headers.map(h => h.name).join(";");
    const rows = dt.rows.map(row => row.cells.map(c => c.value).join(";")).join("\r\n");
    fileDownload(headers + "\r\n" + rows, model.name + '.csv')
  }

  return (
    <Toolbar className={classes.root}>
      {edit === "META" ? <MetaEditor onChange={onChange} model={model} onClose={() => setEdit(undefined)}>{dt}</MetaEditor> : null}
      {edit === "ROWS-COLUMNS" ? <RowsColumns onChange={onChange} onClose={() => setEdit(undefined)}>{dt}</RowsColumns> : null}
      {edit === "CSV_UPLOAD" ? <UploadCSV onChange={onChange} onClose={() => setEdit(undefined)}>{dt}</UploadCSV> : null}

      <Typography className={classes.title} variant="h6" >
        {dt.hitPolicy} / {dt.name} / {dt.description}
      </Typography>

      <ButtonGroup variant="text">
        <Tooltip title={<FormattedMessage id='dt.toolbar.dt.add.input.column' />}>
          <Button color="inherit" size="small" onClick={() => onChange([{ type: 'ADD_HEADER_IN', id: "in-" + dt.headers.length + 1 }])}>
            <DoubleArrowRoundedIcon sx={{ transform: "rotate(-180deg)" }} />
          </Button>
        </Tooltip>
        <Tooltip title={<FormattedMessage id='dt.toolbar.dt.add.output.column' />}>
          <Button color="secondary" size="small" onClick={() => onChange([{ type: 'ADD_HEADER_OUT', id: "out-" + dt.headers.length + 1 }])}>
            <DoubleArrowRoundedIcon />
          </Button>
        </Tooltip>

        <Tooltip title={<FormattedMessage id='dt.toolbar.dt.add.row' />}>
          <Button color="primary" size="small" onClick={() => onChange([{ type: 'ADD_ROW', id: "" }])}>
            <DoubleArrowRoundedIcon sx={{ transform: "rotate(90deg)" }} />
          </Button>
        </Tooltip>

        <Tooltip title={<FormattedMessage id='dt.toolbar.dt.hitpolicy.description' />}>
          <Button color="inherit" size="small" onClick={() => setEdit("META")}>
            <EditIcon />
          </Button>
        </Tooltip>

        <Tooltip title={<FormattedMessage id='dt.toolbar.csv.download' />}>
          <Button color="inherit" size="small" onClick={saveCsv}>
            <FileDownloadDoneIcon />
          </Button>
        </Tooltip>

        <Tooltip title={<FormattedMessage id='dt.toolbar.csv.upload' />}>
          <Button color="inherit" size="small" onClick={() => setEdit("CSV_UPLOAD")}>
            <UploadIcon />
          </Button>
        </Tooltip>

        <Tooltip title={<FormattedMessage id='dt.toolbar.dt.organize.rows.columns' />}>
          <Button color="inherit" size="small" onClick={() => setEdit("ROWS-COLUMNS")}>
            <CompareArrowsRoundedIcon sx={{ transform: "rotate(90deg)" }} />
            <CompareArrowsRoundedIcon />
          </Button>
        </Tooltip>

      </ButtonGroup>
    </Toolbar>
  );
};

export type { DecisionTableToolbarProps };
export { DecisionTableToolbar };

