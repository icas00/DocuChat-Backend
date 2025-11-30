import React, { useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../utils/cn'; // We'll need a utility for class merging

export const Button = ({ children, className, variant = 'primary', ...props }) => {
    const ref = useRef(null);
    const [position, setPosition] = useState({ x: 0, y: 0 });

    const handleMouseMove = (e) => {
        const { clientX, clientY } = e;
        const { left, top, width, height } = ref.current.getBoundingClientRect();
        const x = (clientX - (left + width / 2)) * 0.2; // Magnetic pull strength
        const y = (clientY - (top + height / 2)) * 0.2;
        setPosition({ x, y });
    };

    const handleMouseLeave = () => {
        setPosition({ x: 0, y: 0 });
    };

    const variants = {
        primary: 'bg-gradient-to-r from-aurora-500 to-aurora-glow text-white shadow-[0_0_20px_rgba(14,165,233,0.3)] hover:shadow-[0_0_30px_rgba(14,165,233,0.5)]',
        secondary: 'bg-glass-100 border border-glass-200 text-white hover:bg-glass-200',
        ghost: 'bg-transparent text-aurora-300 hover:text-white',
    };

    return (
        <motion.button
            ref={ref}
            onMouseMove={handleMouseMove}
            onMouseLeave={handleMouseLeave}
            animate={{ x: position.x, y: position.y }}
            transition={{ type: 'spring', stiffness: 150, damping: 15, mass: 0.1 }}
            className={cn(
                'relative px-6 py-3 rounded-xl font-medium text-sm transition-colors duration-300 flex items-center justify-center gap-2',
                variants[variant],
                className
            )}
            {...props}
        >
            {children}
        </motion.button>
    );
};
