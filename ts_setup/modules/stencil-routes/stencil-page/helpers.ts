import { StencilApi } from "@dxs-ts/stencil-api";


export function parsePageTitle(page: StencilApi.Page): string | undefined {
  const normalized = page.body.content.replaceAll('\r\n', '\n').replaceAll('\r', '\n');
  const firstLine = normalized.split('\n')[0];
  return firstLine.startsWith("# ") ? firstLine.slice(2) : '--';
}
