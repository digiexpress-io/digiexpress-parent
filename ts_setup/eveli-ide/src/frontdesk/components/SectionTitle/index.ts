import { SectionTitle as SectionTitleMain } from './SectionTitle';
import { SectionTitleActions } from './SectionTitleActions';
import { SectionTitleHeader } from './SectionTitleHeader';

export const SectionTitle = Object.assign(SectionTitleMain, {
  Header: SectionTitleHeader,
  Actions: SectionTitleActions,
});
