
import React from 'react';
import { Box, useTheme, alpha, Theme } from '@mui/material';
import { TaskCardId, useCardConfig } from './CardConfigContext';


interface DraggableCardWrapperProps {
  id: string;
  draggingId: string | null;
  isDragging: boolean;
  isDropTarget: boolean;
  onDragStart: (e: React.DragEvent<HTMLDivElement>, id: string) => void;
  onDragEnd: () => void;
  onDrop: (e: React.DragEvent<HTMLDivElement>, id: string) => void;
  onDragOver: (e: React.DragEvent<HTMLDivElement>) => void;
  onDragLeave: (e: React.DragEvent<HTMLDivElement>, id: string) => void;
  children: React.ReactNode;
}

function getBorder(theme: Theme, isDragging: boolean, isDropTarget: boolean) {
  if (isDragging) {
    return `2px dashed ${theme.palette.primary.main}`;
  }
  if (isDropTarget) {
    return `1px solid ${theme.palette.divider}`;
  }
  return 'none';
}



export const DraggableCardWrapper: React.FC<DraggableCardWrapperProps> = ({
  id,
  draggingId,
  isDragging,
  isDropTarget,
  onDragStart,
  onDragEnd,
  onDrop,
  onDragOver,
  onDragLeave,
  children
}) => {
  const theme = useTheme();
  const isDraggingStartedOverAllCards: boolean = !!draggingId; 
  
  return (
    <Box
      id={id}
      //draggable
      onDragStart={(e) => onDragStart(e, id)}
      onDragEnd={onDragEnd}
      onDrop={(e) => onDrop(e, id)}
      onDragOver={onDragOver}
      onDragLeave={(e) => onDragLeave(e, id)}
      sx={{
        height: '100%',
        transform: isDraggingStartedOverAllCards && (isDragging || isDropTarget) ? 'scale(0.95)' : 'scale(1)',
        transition: 'transform 0.2s ease, border 0.1s ease',
        border: getBorder(theme, isDraggingStartedOverAllCards && isDragging, isDraggingStartedOverAllCards && isDropTarget),
        borderRadius: theme.spacing(1),
        opacity: isDraggingStartedOverAllCards && isDragging ? 0.6 : 1,
        boxShadow: isDraggingStartedOverAllCards && isDropTarget ? `0 0 6px 3px ${alpha(theme.palette.divider, 0.50)}` : 'none'
      }}
    >
      {children}
    </Box>
  );
};

export function useDragCardController<T extends TaskCardId>() {

  const { cardOrder: items, setCardOrder: setItems } = useCardConfig();

  const theme = useTheme<Theme>();
  const [draggingId, setDraggingId] = React.useState<T | null>(null);
  const [dropTargetId, setDropTargetId] = React.useState<T | null>(null);
  const draggedId = React.useRef<T | null>(null);


  const getDragHandlePropsForId = (id: T) => ({
    draggable: true,
    onDragStart: (e: React.DragEvent<HTMLDivElement>) => handleDragStart(e, id),
    onDragEnd: handleDragEnd,
  });


  function startAutoScroll() {
    const scrollMargin = 80; // px distance from top/bottom to trigger
    const maxSpeed = 15;     // px per frame (lower than before)
    let scrollSpeed = 0;     // current frame speed
    let scrollDirection = 0; // -1 = up, 1 = down, 0 = none
    let rafId: number | null = null;

    const handleDragOver = (event: DragEvent) => {
      const { clientY } = event;
      const viewportHeight = window.innerHeight;

      scrollDirection = 0;
      scrollSpeed = 0;

      if (clientY < scrollMargin) {
        // Cursor near top → scroll up
        scrollDirection = -1;
        const distance = Math.max(clientY, 0);
        scrollSpeed = ((scrollMargin - distance) / scrollMargin) * maxSpeed;
      } else if (viewportHeight - clientY < scrollMargin) {
        // Cursor near bottom → scroll down
        scrollDirection = 1;
        const distance = Math.max(viewportHeight - clientY, 0);
        scrollSpeed = ((scrollMargin - distance) / scrollMargin) * maxSpeed;
      }
    };

    const step = () => {
      if (scrollDirection !== 0 && scrollSpeed > 0) {
        window.scrollBy(0, scrollDirection * scrollSpeed);
      }
      rafId = requestAnimationFrame(step);
    };

    window.addEventListener('dragover', handleDragOver);
    rafId = requestAnimationFrame(step);

    return () => {
      window.removeEventListener('dragover', handleDragOver);
      if (rafId) cancelAnimationFrame(rafId);
    };
  }


  let stopAutoScroll: (() => void) | null = null;

  const handleDragStart = (event: React.DragEvent<HTMLDivElement>, id: T) => {
    draggedId.current = id;
    setDraggingId(id);

    const target = event.currentTarget;
    const clone = target.cloneNode(true) as HTMLElement;

    clone.style.border = `2px dashed ${theme.palette.primary.main}`;
    clone.style.height = 'fit-content';
    clone.style.width = 'fit-content';
    clone.style.background = theme.palette.background.default;

    document.body.appendChild(clone);
    event.dataTransfer.setDragImage(clone, 10, 10);

    setTimeout(() => {
      document.body.removeChild(clone);
    }, 0);

    stopAutoScroll = startAutoScroll();
  };

  const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    const id = (event.currentTarget as HTMLElement).id as T;
    if (id !== draggingId) {
      setDropTargetId(id);
    }
  };

  const handleDrop = (event: React.DragEvent<HTMLDivElement>, id: T) => {
    event.preventDefault();

    const fromId = draggedId.current;
    if (!fromId || fromId === id) return;

    const newOrder = [...items];
    const fromIndex = newOrder.indexOf(fromId);
    const toIndex = newOrder.indexOf(id);

    [newOrder[fromIndex], newOrder[toIndex]] = [newOrder[toIndex], newOrder[fromIndex]];
    setItems(newOrder);

    resetDragState();
  };

  const handleDragLeave = (event: React.DragEvent<HTMLDivElement>, id: T) => {
    const related = event.relatedTarget as HTMLElement | null;
    const leavingCard = !(event.currentTarget as HTMLElement).contains(related);

    if (dropTargetId === id && leavingCard) {
      setDropTargetId(null);
    }
  };
  const handleDragEnd = () => {
    resetDragState();
    if (stopAutoScroll) {
      stopAutoScroll();
      stopAutoScroll = null;
    }
  };

  const resetDragState = () => {
    draggedId.current = null;
    setDraggingId(null);
    setDropTargetId(null);
  };

  const getDragPropsForId = (id: T) => ({
    id,
    isDragging: draggingId === id,
    isDropTarget: dropTargetId === id,
    onDragStart: (e: React.DragEvent<HTMLDivElement>) => handleDragStart(e, id),
    onDragEnd: handleDragEnd,
    onDrop: (e: React.DragEvent<HTMLDivElement>) => handleDrop(e, id),
    onDragOver: handleDragOver,
    onDragLeave: (e: React.DragEvent<HTMLDivElement>) => handleDragLeave(e, id),
  });

  return {
    getDragPropsForId, getDragHandlePropsForId,

    draggingId
  };
}

