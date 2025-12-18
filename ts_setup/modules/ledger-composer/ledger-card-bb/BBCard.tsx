import React from 'react';
import { alpha, Box, Collapse, Divider, IconButton, styled, Typography, useTheme } from '@mui/material';

import { AccountBalanceWallet as AccountBalanceWalletIcon } from '@mui/icons-material';
import { ExpandLess as ExpandLessIcon, ExpandMore as ExpandMoreIcon } from '@mui/icons-material';

import { useIntl } from 'react-intl';

import { LedgerCardDataListExpander, LedgerCardDataRowText, LedgerCardTransitivesRow, StartAdornmentIcon } from '../ledger-card';
import { LedgerApi, useLedger } from '@dxs-ts/ledger-api';
import { LedgerCard } from '../ledger-card';
import { useLedgerCard } from '../ledger-card-factory';
import { formatAnyDateShort } from '@dxs-ts/xui-datetime';
import { LedgerFormatter } from '../ledger-formatter';




export const LedgerCardBB: React.FC<{}> = () => {
  const intl = useIntl();
  const { ledgerContainer } = useLedger();
  const formatter = LedgerFormatter;

  const { blackBooks, blackBookDetails } = ledgerContainer;

  const commonProps = useLedgerCard();

  return (
    <LedgerCard title={intl.formatMessage({ id: 'ledgercard.title.ledgerBlackBooks' })}
      {...commonProps}
      isMenu
      showFlashyToggle={true}
      showEditOnMenu={true}
      showEditButton={true}
      showReviewOnMenu={true}
      onEdit={commonProps.onEditOpen}
      startAdornmentIcon={<StartAdornmentIcon icon={AccountBalanceWalletIcon} />}
      onDoubleClick={commonProps.onEditOpen}
    //editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
    >
      <LedgerCardDataListExpander
        columns={[
          { key: "description", label: intl.formatMessage({ id: 'ledgercard.blackBook.description' }), width: "40%" },
          { key: "amount", label: intl.formatMessage({ id: 'ledgercard.blackBook.amount' }), width: "20%" },
          { key: "bookDate", label: intl.formatMessage({ id: 'ledgercard.blackBook.bookDate' }), width: "20%" },
          { key: "type", label: intl.formatMessage({ id: 'ledgercard.blackBook.type' }), width: "20%" },
        ]}

        rows={blackBooks
          .map(book => ({
            columns: {
              description: book.bookDescription ?? "-",
              amount: formatter.monetary_value(book.bookAmount),
              bookDate: formatAnyDateShort(book.bookDate),
              type: book.bookType ?? "-"
            },
            expanded: blackBookDetails[book.id] ? <BBCardDetail book={book} /> : undefined
          }))}

      />
    </LedgerCard>
  );
}


const BBCardDetail: React.FC<{ book: LedgerApi.BlackBook }> = ({ book }) => {
  const { ledgerContainer } = useLedger();
  return (
    <>
      {ledgerContainer.blackBookDetails[book.id].map((detail) => (
        <BBDetailBlock key={detail.id} detail={detail} />
      ))}
    </>
  );
};


const BBDetailBlock: React.FC<{ detail: LedgerApi.BlackBookDetail }> = ({ detail }) => {
  const intl = useIntl();
  const theme = useTheme();
  const formatter = LedgerFormatter;
  const [expanded, setExpanded] = React.useState(false);

  return (
    <Box mb={2}>
      <StyledExpanderBoxPrimary>
        <LedgerCardDataRowText
          label={intl.formatMessage({ id: 'ledgercard.blackBook.detail.detailType' })}
          value={detail.detailType}
        />
        <LedgerCardDataRowText
          label={intl.formatMessage({ id: 'ledgercard.blackBook.detail.detailDescription' })}
          value={detail.detailDescription ?? "--"}
        />
        <LedgerCardDataRowText
          label={intl.formatMessage({ id: 'ledgercard.blackBook.detail.detailAmount' })}
          value={formatter.monetary_value(detail.detailAmount)}
        />
        <LedgerCardDataRowText
          label={intl.formatMessage({ id: 'ledgercard.blackBook.detail.inflowAmount' })}
          value={formatter.monetary_value(detail.detailInflowAmount)}
        />
        <LedgerCardDataRowText
          label={intl.formatMessage({ id: 'ledgercard.blackBook.detail.outflowAmount' })}
          value={formatter.monetary_value(detail.detailOutflowAmount)}
        />
        <LedgerCardDataRowText
          label={intl.formatMessage({ id: 'ledgercard.blackBook.detail.deltaAmount' })}
          value={formatter.monetary_value(detail.detailDeltaAmount)}
        />
        <LedgerCardTransitivesRow {...detail.transitives} />
      </StyledExpanderBoxPrimary>

      <Box display="flex" alignItems="center" mt={1}>
        <IconButton size="small" onClick={() => setExpanded(v => !v)} sx={{ mr: 0.5 }}>
          {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
        </IconButton>
        <Typography
          variant="subtitle2"
          fontWeight="bold"
          sx={{ cursor: 'pointer', color: theme.palette.primary.main }}
          onClick={() => setExpanded(v => !v)}
        >
          {expanded ? intl.formatMessage({ id: 'ledgercard.blackBook.detail.expander.collapseBreakdown' }) : intl.formatMessage({ id: 'ledgercard.blackBook.detail.expander.expandBreakdown' })}
        </Typography>
      </Box>

      <Collapse in={expanded} timeout="auto" unmountOnExit>
        <StyledExpanderBoxSecondary>
          <Typography fontWeight={500}>
            {intl.formatMessage({ id: 'ledgercard.blackBook.detail.breakdown' })}
          </Typography>
          <Divider sx={{ my: theme.spacing(0.5) }} />
          {((detail.detailBody?.logs ?? []) as string[]).map((log, i) => (
            <Typography key={i} variant="subtitle2">
              {formatter.formula_value(log)}
            </Typography>
          ))}

        </StyledExpanderBoxSecondary>
      </Collapse>
    </Box>
  );
};



const StyledExpanderBoxPrimary = styled(Box)(({ theme }) => ({
  padding: theme.spacing(1),
  borderLeft: `2px solid ${alpha(theme.palette.primary.main, 0.8)}`,
  backgroundColor: alpha(theme.palette.primary.main, 0.05),
}));


const StyledExpanderBoxSecondary = styled(Box)(({ theme }) => ({
  padding: theme.spacing(1),
  marginTop: theme.spacing(1),
  borderLeft: `2px solid ${alpha(theme.palette.info.dark, 0.8)}`,
  backgroundColor: alpha(theme.palette.info.dark, 0.05),
}));
