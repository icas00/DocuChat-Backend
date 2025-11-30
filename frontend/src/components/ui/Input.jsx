import React from 'react';
import { cn } from '../../utils/cn';

export const Input = React.forwardRef(({ className, ...props }, ref) => {
    return (
        <input
            ref={ref}
            className={cn(
                'w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white placeholder-gray-400 focus:outline-none focus:border-pastel-mint focus:ring-1 focus:ring-pastel-mint transition-all duration-300',
                className
            )}
            {...props}
        />
    );
});

Input.displayName = 'Input';
