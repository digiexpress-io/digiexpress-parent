import React from 'react';
import { alpha, Box, Collapse, IconButton, styled, Typography, useTheme } from '@mui/material';

import { AccountBalanceWallet as AccountBalanceWalletIcon } from '@mui/icons-material';
import { ExpandLess as ExpandLessIcon, ExpandMore as ExpandMoreIcon } from '@mui/icons-material';

import { useIntl } from 'react-intl';

import { LedgerCardDataListExpander, StartAdornmentIcon } from '../ledger-card';
import { LedgerApi, useLedger } from '@dxs-ts/ledger-api';
import { LedgerCard } from '../ledger-card';
import { useLedgerCard } from '../ledger-card-factory';
import { formatAnyDateShort } from '@dxs-ts/xui-datetime';




export const LedgerCardBB: React.FC<{}> = () => {
  const intl = useIntl();
  const { ledgerContainer } = useLedger();
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
              amount: book.bookAmount.toString() ?? "-",
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
  const theme = useTheme();
  const { ledgerContainer } = useLedger();
  const [expandedExtraRows, setExpandedExtraRows] = React.useState<Record<number, boolean>>({});


  const toggleExtraRow = (index: number) => {
    setExpandedExtraRows(prev => ({ ...prev, [index]: !prev[index] }));
  }

  return ledgerContainer.blackBookDetails[book.id].map((detail, i) => (
    <div key={i}>
      <StyledExpanderBox>
        <Typography variant='subtitle2'>
          {detail.detailType}
        </Typography>
        <Typography variant='subtitle2'>
          {detail.detailAmount}
        </Typography>

        <Typography variant='subtitle2'>
          {detail.detailInflowAmount}
        </Typography>

        <Typography variant='subtitle2'>
          {detail.detailOutflowAmount}
        </Typography>

        <Typography variant='subtitle2'>
          {detail.detailDeltaAmount}
        </Typography>

        <Typography variant='subtitle2'>
          {detail.detailDescription}
        </Typography>

        <LedgerCardTransitivesRow {...detail.transitives} />
      </StyledExpanderBox>
      <Box display="flex" alignItems="center" mt={1}>
        <IconButton size="small" onClick={() => toggleExtraRow(i)} sx={{ mr: 0.5 }}>
          {expandedExtraRows[i] ? <ExpandLessIcon /> : <ExpandMoreIcon />}
        </IconButton>
        <Typography variant="subtitle2" fontWeight='bold' sx={{ cursor: "pointer", color: theme.palette.primary.main }} onClick={() => toggleExtraRow(i)}>
          {expandedExtraRows[i] ? 'view less' : 'view more'}
        </Typography>
      </Box>

      <Collapse in={expandedExtraRows[i]} timeout="auto" unmountOnExit>
        <StyledExpanderBox>

          {((detail.detailBody?.logs ?? []) as string[])
            .map((log, key) => <div key={key}><Typography variant="subtitle2">{log}</Typography></div>)}
        </StyledExpanderBox>
      </Collapse>
    </div>));
}



const StyledExpanderBox = styled(Box)(({ theme }) => ({
  paddingLeft: theme.spacing(1),
  paddingTop: theme.spacing(1),
  paddingBottom: theme.spacing(1),
  marginLeft: theme.spacing(5),
  borderLeft: `2px solid ${alpha(theme.palette.primary.main, 0.8)}`,
  backgroundColor: alpha(theme.palette.primary.main, 0.05),
}));


const LedgerCardTransitivesRow: React.FC<{ createdAt?: string, updatedAt?: string }> = ({ createdAt, updatedAt }) => {
  const intl = useIntl();
  const theme = useTheme();

  return (
    <Box display='flex' gap={theme.spacing(1)} marginTop={theme.spacing(1)} justifyContent='end'>
      <Typography variant='caption'>{intl.formatMessage({ id: 'ledgercard.transitives.createdAt' })}{": "}{createdAt}</Typography>
      <Typography variant='caption'>{intl.formatMessage({ id: 'ledgercard.transitives.updatedAt' })}{": "}{updatedAt}</Typography>
    </Box>
  )
}
