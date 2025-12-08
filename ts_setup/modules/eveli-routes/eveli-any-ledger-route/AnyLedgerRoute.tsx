import React from 'react';
import { Box } from '@mui/material';
import { useNavigate } from '@tanstack/react-router'
import { DateTime } from "luxon";

import { useFetch } from '@dxs-ts/envir-fetch';
import { DatePicker as XuiDatePicker, DateTimeFormatter as XuiDateTimeFormatter } from "@dxs-ts/xui-datetime";
import { useIam, useTenantConfigFeatures } from '@dxs-ts/eveli-api';

import { LedgerBackendProvider, LedgerBackendProviderProps } from '@dxs-ts/ledger-api';


export const AnyLedgerRoute: React.FC<{children: React.ReactNode}> = ({ children }) => {

  const [open, setOpen] = React.useState(false);
  const navigate = useLedgerNavigate({ setTaskCreateOpen: setOpen });
  const permissions = useLedgerPermissions();
  const persistence = useLedgerPersistence();
  const features = useLedgerFeatures();
  const { user } = useIam();

  const currentUser = React.useMemo(() => ({
    name: user.name || "",
    email: user.email || ""
  }), [user.name, user?.email]);

  const SlotDateTimePicker: React.FC<{
    value?: string | Date | null;
    onChange?: (d: Date | null) => void;
    onKeyDown?: React.KeyboardEventHandler;
    readonly?: boolean;
    fullWidth?: boolean;
    size?: "small" | "medium";
    label?: React.ReactNode;
  }> = ({ value, onChange, onKeyDown, readonly, fullWidth = true, size = "small" }) => {

    const normalized: Date | null =
      typeof value === "string"
        ? value
          ? DateTime.fromISO(value).toJSDate()
          : null
        : value ?? null;
  
    return (
      <Box onKeyDown={onKeyDown}>
        <XuiDatePicker
          fullWidth={fullWidth}
          size={size}
          value={normalized}
          onChange={readonly ? () => {} : (d) => onChange?.(d)}
          sx={{ pointerEvents: readonly ? "none" : "auto" }}
        />
      </Box>
    );
  };  

  const SlotDateTimeFormatter: React.FC<{ value: any; variant?: "text" }> = ({
    value,
    variant,
  }) => <XuiDateTimeFormatter value={value} variant={variant} />;

  return (
    <LedgerBackendProvider
      deps={[]}
      roles={[]}
      navigate={navigate}
      permissions={permissions}
      persistence={persistence}
      currentUser={currentUser}
      features={features}
      slots={{
        DateTimeFormatter: SlotDateTimeFormatter,
        DateTimePicker: SlotDateTimePicker,
      }}>
        {children}
      </LedgerBackendProvider>)
}


function useLedgerNavigate(props: { setTaskCreateOpen: (open: boolean) => void}): LedgerBackendProviderProps['navigate'] {
  const navigate = useNavigate();
  const tenant = useTenantConfigFeatures();

  return {
    findAllLedgers: () => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/ledgers'
    }),
    openOneLedger: (ledgerId: string) => navigate({
      from: '/secured/$locale/worker',
      to:'/secured/$locale/worker/ledgers/$ledgerId',
      params: { ledgerId }
    }),
  }
}

function useLedgerFeatures(): LedgerBackendProviderProps['features'] {
  const tenant = useTenantConfigFeatures()
  return {   
  }
}
function useLedgerPermissions(): LedgerBackendProviderProps['permissions'] {
  return []
}

function useLedgerPersistence(): LedgerBackendProviderProps['persistence'] {
  const { findAllLedgers, getOneLedger } = useFetch('worker/rest/api/ledgers.GET', {});

  const unit: LedgerBackendProviderProps['persistence'] = {
    findAllLedgers,
    getOneLedger
  }
  return unit;
}