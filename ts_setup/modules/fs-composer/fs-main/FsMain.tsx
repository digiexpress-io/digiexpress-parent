import React from 'react';
import { Tooltip, Badge } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsu } from '@dxs-ts/fs-api';
import { useOwnerState } from './useOwnerState';
import { FsMainRoot, FsSaveButtonRoot, FsToolbarButtonRoot, useUtilityClasses } from './useUtilityClasses';
import { FsMainDefaultBackground } from './FsMainDefaultBackground';
import { OwnerState } from './useOwnerState';
import { ContentPanel } from './ContentPanel';
import { FsIcons } from '../fs-theme';




export const FsMain: React.FC = () => {
  const ownerState = useOwnerState();
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <FsMainRoot ownerState={ownerState} className={classes.root}>
      <div key={ownerState.activeTabId} className={classes.leftPanel} >
        <Content ownerState={ownerState} />
      </div>

      <div className={classes.divider} />

      <div className={`${classes.rightPanel} ${ownerState.isRightPanelOpen ? classes.rightPanelOpen : classes.rightPanelClosed}`}>
        <ContentPanel ownerState={ownerState} className={classes.rightPanelContent} />
      </div>

      <div className={classes.toolbar}>
        <Tooltip key={'toggle-panel'}
          title={ownerState.isRightPanelOpen ?
            intl.formatMessage({ id: 'fs.main.tooltip.togglePanelCollapse' }) :
            intl.formatMessage({ id: 'fs.main.tooltip.togglePanelExpand' })}
          placement="left" arrow>
          <FsToolbarButtonRoot onClick={ownerState.toggleRightPanel} ownerState={{
            isEnabled: true,
            isSelected: false
          }}>{ownerState.isRightPanelOpen ? <FsIcons.CollapseAll /> : <FsIcons.ExpandAll />}</FsToolbarButtonRoot>
        </Tooltip>

        <PanelButton
          id='properties'
          ownerState={ownerState}
          icon={FsIcons.Info}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.properties' })}
        />
        {/* TODO
        <PanelButton
          id='references'
          ownerState={ownerState}
          icon={FsIcons.Tree}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.references' })}
        /> */}
        <PanelButton
          id='debug'
          ownerState={ownerState}
          icon={FsIcons.Debug}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.debug' })}
        />
        <PanelButton
          id='errors'
          ownerState={ownerState}
          icon={FsIcons.Error}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.errors' })}
        />
        <PanelButton
          id='preview'
          ownerState={ownerState}
          icon={FsIcons.Preview}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.preview' })}
        />
        <PanelButton
          id='history'
          ownerState={ownerState}
          icon={FsIcons.History}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.history' })}
        />
        <PanelButton
          id='help'
          ownerState={ownerState}
          icon={FsIcons.Help}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.help' })}
        />
        <PanelButton
          id='article-order'
          ownerState={ownerState}
          icon={FsIcons.ArticleOrder}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.articleOrder' })}
        />
        <PanelButton
          id='article-locale-overview'
          ownerState={ownerState}
          icon={FsIcons.Language}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.articleLocaleOverview' })}
        />
        <PanelButton
          id='stats'
          ownerState={ownerState}
          icon={FsIcons.Stats}
          tooltip={intl.formatMessage({ id: 'fs.main.tooltip.stats' })}
        />

        <SaveButton ownerState={ownerState} />


      </div>
    </FsMainRoot>
  );
}

const Content: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const dirent = ownerState.activeDirent;
  if (dirent) {
    const widget = ownerState.activeWidget!;
    return (<widget.views.UpdateView direntId={dirent.id} />)
  } else if (ownerState.activeTab?.type === 'create') {
    const widget = ownerState.activeWidget!;
    return <widget.views.CreateView />;
  }
  return (<FsMainDefaultBackground />);
}


const SaveButton: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const intl = useIntl();
  const tooltip = intl.formatMessage({ id: 'fs.main.tooltip.changes' });

  const { allChanges } = useFsu();
  const unsavedCount = allChanges.filter(c => c.isDirty).length;

  return (
    <Tooltip key={'save'} title={tooltip} placement="left" arrow >
      <FsSaveButtonRoot ownerState={{ unsavedCount }} onClick={() => ownerState.onViewChange('changes')}>
        <Badge badgeContent={unsavedCount} color="error">
          <FsIcons.Save />
        </Badge>
      </FsSaveButtonRoot>
    </Tooltip>
  )
}

const PanelButton: React.FC<{
  id: Fs.SecondaryView,
  tooltip: string,
  icon: React.ElementType,
  ownerState: OwnerState,
}> = ({ id, tooltip, ownerState, icon: Icon }) => {

  function handleOnClick() {
    const isEnabled = ownerState.activeWidget?.meta.supportedViews.includes(id as any);
    if (isEnabled) {
      ownerState.onViewChange(id as any)
    }
  }
  const isSelected: boolean = ownerState.selectedView === id;
  const isEnabled: boolean = ownerState.activeWidget?.meta.supportedViews.includes(id as any) ?? false;


  return (
    <Tooltip key={id} title={tooltip} placement="left" arrow>
      <FsToolbarButtonRoot onClick={handleOnClick} ownerState={{ isEnabled, isSelected }}>
        <Icon />
      </FsToolbarButtonRoot>
    </Tooltip>);
}

