import React from 'react';
import { Box } from '@mui/material';
import { useNavigate } from '@tanstack/react-router'
import { DateTime } from "luxon";

import { useFetch } from '@dxs-ts/envir-fetch';
import { DatePicker as XuiDatePicker, DateTimeFormatter as XuiDateTimeFormatter } from "@dxs-ts/xui-datetime";
import { useIam, useTenantConfigFeatures } from '@dxs-ts/eveli-api';

import { ContractBackendProvider, ContractBackendProviderProps } from '@dxs-ts/contract-api';


export const AnyContractRoute: React.FC<{children: React.ReactNode}> = ({ children }) => {

  const [open, setOpen] = React.useState(false);
  const navigate = useContractNavigate({ setTaskCreateOpen: setOpen });
  const permissions = useContractPermissions();
  const persistence = useContractPersistence();
  const features = useContractFeatures();
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
    <ContractBackendProvider
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
      </ContractBackendProvider>)
}


function useContractNavigate(props: { setTaskCreateOpen: (open: boolean) => void}): ContractBackendProviderProps['navigate'] {
  const navigate = useNavigate();
  const tenant = useTenantConfigFeatures();

  return {
    findAllContracts: () => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/contracts'
    }),
    openOneContract: (contractId: string) => navigate({
      from: '/secured/$locale/worker',
      to:'/secured/$locale/worker/contracts/$contractId',
      params: { contractId }
    }),
  }
}

function useContractFeatures(): ContractBackendProviderProps['features'] {
  const tenant = useTenantConfigFeatures()
  return {   
  }
}
function useContractPermissions(): ContractBackendProviderProps['permissions'] {
  return []
}

function useContractPersistence(): ContractBackendProviderProps['persistence'] {
  const { findAllContracts, getOneContract } = useFetch('worker/rest/api/contracts.GET', {});

  const unit: ContractBackendProviderProps['persistence'] = {
    findAllContracts,
    getOneContract
  }
  return unit;
}