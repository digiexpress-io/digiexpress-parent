import React from 'react';
import { Box, Divider, Stack, Typography } from '@mui/material';


import { HandshakeOutlined as HandshakeOutlinedIcon } from '@mui/icons-material';
import { CalendarMonthOutlined as CalendarMonthOutlinedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';


import { ContractCard, ContractCardDataRowElement, ContractCardId, useCardConfig, ContractCardDataRowText, useContractCardThemeConfig, StartAdornmentIcon, ContractCardDataRowParty, ContractCardTransitivesRow } from '../contract-card';
import { useContract } from '@dxs-ts/contract-api';


export type FactoryCardId = 'contract_main' | 'contract_parties' | 'coverages' | 'payment_plans' | 'investment_plans';

export const CONTRACT_CARD_IDS: FactoryCardId[] = [
  'contract_main',
  'contract_parties',
  'coverages',
  'payment_plans',
  'investment_plans'
];

const defaultExpandedCards: FactoryCardId[] = ['contract_main', 'investment_plans'];

export const ContractCardFactory: React.FC<{ cardId: ContractCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  const { contractContainer } = useContract();
  const { contract, parties, coverages, paymentPlans, invPlans } = contractContainer;

  console.log(contractContainer)


  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCardConfig();

  const styleConfig = useContractCardThemeConfig();
  const style = styleConfig[cardTheme];


  const commonProps = {
    id: cardId,
    styleVariant: cardTheme,
    isFlashy: isCardFlashy(cardId),
    isExpanded: expandedCards.find(target => target.cardId === cardId) ? isCardExpanded(cardId) : defaultExpandedCards.includes(cardId),
    onToggleFlashy: () => toggleCardFlashy(cardId),
    onToggleExpanded: () => {
      const current = expandedCards.find(target => target.cardId === cardId);
      const isDefault = defaultExpandedCards.includes(cardId)
      if (isDefault) {
        toggleCardExpanded(cardId, current ? undefined : false);
      } else {
        toggleCardExpanded(cardId, current ? undefined : true);
      }
    },
    onReview: toggleReview,

  };

  const isEditOpen = cardId === editingCardId;

  function handleEdit() {
    setEditCard(cardId);
  }
  function handleEditClose() {
    setEditCard(undefined);
  }


  switch (cardId) {

    case 'contract_main':
      return (
        <ContractCard title={intl.formatMessage({ id: 'contractcard.contractMain.title' }, { contractId: contract.contractNumber })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<></>)}
          startAdornmentIcon={<StartAdornmentIcon icon={HandshakeOutlinedIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.category' })} value={contract.contractData?.category} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.status' })} value={contract.contractStatus} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.productName' })} value={contract.contractData?.productName} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.productCode' })} value={contract.contractData?.productCode} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.issueDate' })} value={formatAnyDateShort(contract.contractData?.issueDate)} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.maturityDate' })} value={formatAnyDateShort(contract.contractMaturityDate)} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.annualMaxContribution' })} value={contract.contractData?.limits.annualMaxContribution} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.contractMinValue' })} value={contract.contractData?.limits.contractMinValue} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.partialWithdrawalMin' })} value={contract.contractData?.limits.partialWithdrawalMin} style={style} />
        </ContractCard>
      );

    case 'contract_parties':
      return (<ContractCard title={intl.formatMessage({ id: 'contractcard.contractParties.title' })}
        {...commonProps}
        isMenu
        titleNotifier={parties.length}
        onDoubleClick={handleEdit}
        onEdit={handleEdit}
        editDialog={editingCardId === cardId && (<></>)}
        startAdornmentIcon={<StartAdornmentIcon icon={CalendarMonthOutlinedIcon} />}

        showFlashyToggle={true}
        showEditOnMenu={true}
        showEditButton={true}
        showReviewOnMenu={false}
      >
        {parties.map(party => {
          const partyCovers = coverages.filter(c => c.insuredId === party.id).map(c => c.coverageCode)

          return (
            <ContractCardDataRowParty key={party.id} titleLabel={intl.formatMessage({ id: 'contractcard.body.parties.partyType' })} style={style} valueLabel={party.partyType}>
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyFullName' })} value={party.partyData?.fullName ?? "-"} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.dateOfBirth' })} value={formatAnyDateShort(party.partyData?.dateOfBirth)} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyPersonalId' })} value={party.partyData?.personalId ?? "-"} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyAddress.city' })} value={party.partyData?.address.city} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyAddress.street' })} value={party.partyData?.address.street} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyAddress.postalCode' })} value={party.partyData?.address.postalCode} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyAddress.country' })} value={party.partyData?.address.country} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.investmentExperience' })} value={party.partyData?.investmentExperience ?? "--"} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.riskTolerance' })} value={party.partyData?.riskTolerance ?? "--"} style={style} />
              <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.parties.partyCoverages' })} value={partyCovers.join(", ") ?? "--"} style={style} />
              <ContractCardTransitivesRow createdAt={formatAnyDateShort(party.transitives?.createdAt)} updatedAt={formatAnyDateShort(party.transitives?.updatedAt)} />
            </ContractCardDataRowParty>
          )
        })}
      </ContractCard>
      );
    case 'coverages':
      return (<ContractCard title={intl.formatMessage({ id: 'contractcard.coverages.title' })}
        {...commonProps}
        isMenu
        titleNotifier={coverages.length}
        onDoubleClick={handleEdit}
        onEdit={handleEdit}
        editDialog={editingCardId === cardId && (<></>)}
        startAdornmentIcon={<StartAdornmentIcon icon={CalendarMonthOutlinedIcon} />}

        showFlashyToggle={true}
        showEditOnMenu={true}
        showEditButton={true}
        showReviewOnMenu={false}
      >
        {
          coverages.map(cover => {
            const insuredParty = parties.find(p => p.id === cover.insuredId);

            return (
              <div key={cover.id}>
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.coverages.coverageCode' })} value={cover.coverageCode} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.coverages.coverageEffectiveFrom' })} value={formatAnyDateShort(cover.coverageEffectiveFrom)} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.coverages.insuredId' })} value={insuredParty?.partyData?.fullName ?? "--"} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.coverages.coverageType' })} value={cover.coverageType} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.coverages.coverageStatus' })} value={cover.coverageStatus} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.coverages.coverageSumInsured' })} value={cover.coverageSumInsured?.toString()} style={style} />
                <ContractCardTransitivesRow createdAt={formatAnyDateShort(cover.transitives?.createdAt)} updatedAt={formatAnyDateShort(cover.transitives?.updatedAt)} />
              </div>
            )
          })
        }
      </ContractCard>
      );
    case 'payment_plans':
      return (<ContractCard title={intl.formatMessage({ id: 'contractcard.paymentPlans.title' })}
        {...commonProps}
        isMenu
        titleNotifier={paymentPlans.length}
        onDoubleClick={handleEdit}
        onEdit={handleEdit}
        editDialog={editingCardId === cardId && (<></>)}
        startAdornmentIcon={<StartAdornmentIcon icon={CalendarMonthOutlinedIcon} />}

        showFlashyToggle={true}
        showEditOnMenu={true}
        showEditButton={true}
        showReviewOnMenu={false}
      >
        {
          paymentPlans.map(plan => {
            const insuredParty = parties.find(p => p.id === plan.partyId);

            return (
              <div key={plan.id}>
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.paymentPlans.partyName' })} value={insuredParty?.partyData?.fullName ?? "--"} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.paymentPlans.paymentPlanStatus' })} value={plan.paymentPlanStatus} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.paymentPlans.paymentPlanFrequency' })} value={plan.paymentPlanFrequency} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.paymentPlans.paymentPlanAmount' })} value={plan.paymentPlanAmount.toString()} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.transitives.createdAt' })} value={formatAnyDateShort(plan.transitives?.createdAt)} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.transitives.updatedAt' })} value={formatAnyDateShort(plan.transitives?.updatedAt)} style={style} />
                <ContractCardTransitivesRow createdAt={formatAnyDateShort(plan.transitives?.createdAt)} updatedAt={formatAnyDateShort(plan.transitives?.updatedAt)} />

              </div>
            )
          })
        }
      </ContractCard>
      );
    case 'investment_plans':
      return (<ContractCard title={intl.formatMessage({ id: 'contractcard.investmentPlans.title' })}
        {...commonProps}
        isMenu
        titleNotifier={paymentPlans.length}
        onDoubleClick={handleEdit}
        onEdit={handleEdit}
        editDialog={editingCardId === cardId && (<></>)}
        startAdornmentIcon={<StartAdornmentIcon icon={CalendarMonthOutlinedIcon} />}

        showFlashyToggle={true}
        showEditOnMenu={true}
        showEditButton={true}
        showReviewOnMenu={false}
      >
        {
          invPlans.map(plan => {
            return (
              <div key={plan.id}>
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.investmentPlans.invPlanName' })} value={plan.invPlanName} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.investmentPlans.invPlanCode' })} value={plan.invPlanCode} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.investmentPlans.invPlanStatus' })} value={plan.invPlanStatus} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.transitives.createdAt' })} value={formatAnyDateShort(plan.transitives?.createdAt)} style={style} />
                <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.transitives.updatedAt' })} value={formatAnyDateShort(plan.transitives?.updatedAt)} style={style} />
              </div>
            )
          })
        }
      </ContractCard>
      );
    default:
      return null;
  }
}

function formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
}




