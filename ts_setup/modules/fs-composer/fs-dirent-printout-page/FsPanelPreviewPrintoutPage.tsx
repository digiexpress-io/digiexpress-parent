import React from 'react';
import { Button, Typography } from '@mui/material';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useUpdateOwnerState } from './useUpdateOwnerState';

const CompileButton: React.FC<{ direntId: string; onCompile: () => Promise<void> }> = ({ direntId, onCompile }) => {
  const { push, syncResourceLinks, content } = useUpdateOwnerState({ direntId });

  const handleCompile = async () => {
    const capturedContent = content;
    await syncResourceLinks(capturedContent);
    await push();
    await onCompile();
  };

  return <Button onClick={handleCompile}>Compile</Button>;
};

export const FsPanelPreviewPrintoutPage: React.FC = () => {
  const { activeDirent } = useFsNav();
  const { getDirent, selectOptions, compilePrintoutPage } = useFsDirent();

  const [pdfBase64, setPdfBase64] = React.useState('');
  const [pdfUrl, setPdfUrl] = React.useState('');
  const [compilationError, setCompilationError] = React.useState<string | undefined>(undefined);

  const dirent = activeDirent ? getDirent(activeDirent.id) : undefined;
  const pageProps = dirent?.type === 'PRINTOUT_PAGE' ? dirent.props as Fs.PrintoutPageProps : undefined;

  const serviceId = pageProps?.serviceId;
  const localeProps = pageProps?.localeId ? selectOptions.direntProps[pageProps.localeId] as Fs.LanguageProps | undefined : undefined;
  const localeCode = localeProps?.localeCode;

  const onCompile = React.useCallback(async () => {
    if (!serviceId || !localeCode) {
      return;
    }
    setCompilationError(undefined);
    try {
      const result: Fs.PrintoutCompileResult = await compilePrintoutPage(serviceId, localeCode);
      if (result.status === 'OK') {
        setPdfBase64(result.bodyBase64 ?? '');
      } else {
        setCompilationError(result.statusMessage ?? `Compilation failed: ${JSON.stringify(result)}`);
      }
    } catch (err) {
      setCompilationError(String(err));
    }
  }, [compilePrintoutPage, serviceId, localeCode]);

  React.useEffect(() => {
    setPdfBase64('');
    setPdfUrl('');
    setCompilationError(undefined);
    onCompile();
  }, [activeDirent?.id]);

  React.useEffect(() => {
    if (!pdfBase64) {
      return;
    }
    const binary = atob(pdfBase64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    const blob = new Blob([bytes], { type: 'application/pdf' });
    const url = URL.createObjectURL(blob);
    setPdfUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [pdfBase64]);

  return (
    <>
      {activeDirent && <CompileButton direntId={activeDirent.id} onCompile={onCompile} />}
      {compilationError && <Typography color='error'>{compilationError}</Typography>}
      {pdfBase64 && (
        <iframe
          src={pdfUrl}
          title='printout-preview'
          style={{ border: 'none', minHeight: '90vh' }}
        />
      )}
    </>
  );
};
