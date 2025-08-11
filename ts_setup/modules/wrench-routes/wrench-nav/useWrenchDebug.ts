import { HdesApi, WrenchComposerApi as Composer } from "@dxs-ts/wrench-api";
import { useWrenchNav } from "./useWrenchNav";

  export const useWrenchDebug = () => {
    const { onNav } = useWrenchNav();
    const { session, actions } = Composer.useComposer();

    const handleDebugInit = (selected: HdesApi.EntityId) => {
      onNav({ type: 'DEBUG' })

      if (session.debug.selected && session.debug.selected !== selected) {
        const previous = session.debug.values[selected];
        if (previous) {
          actions.handleDebugUpdate(previous);
          return;
        }
      }
      actions.handleDebugUpdate({ inputType: "JSON", selected })
    }
    return { handleDebugInit }
  }
