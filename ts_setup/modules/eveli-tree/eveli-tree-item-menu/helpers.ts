import React from 'react';

export interface PositioningStrategy {
  vertical: 'center' | 'top' | 'bottom';
  shouldExpandUpward: boolean;
}

export function usePositioningStrategy(
  anchorPosition: { top: number; left: number } | undefined,
  menuHeight: number
): PositioningStrategy {
  return React.useMemo(() => {
    if (!anchorPosition) {
      return { vertical: 'center' as const, shouldExpandUpward: false };
    }

    const viewportHeight = window.innerHeight;
    const clickY = anchorPosition.top;
    const menuHalfHeight = menuHeight / 2;

    const spaceAbove = clickY;
    const spaceBelow = viewportHeight - clickY;

    // Check if we can center the menu (enough space above and below)
    const canCenter = spaceAbove >= menuHalfHeight && spaceBelow >= menuHalfHeight;

    let vertical: 'center' | 'top' | 'bottom';
    let shouldExpandUpward: boolean;

    if (canCenter) {
      vertical = 'center';
      shouldExpandUpward = false; // Not relevant for center positioning
    } else {
      // Fall back to up/down positioning based on available space
      shouldExpandUpward = spaceAbove > spaceBelow;
      vertical = shouldExpandUpward ? 'bottom' : 'top';
    }

    return { vertical, shouldExpandUpward };
  }, [anchorPosition, menuHeight]);
}
