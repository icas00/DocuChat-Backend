/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // "Twilight" Palette (Dark Pastel)
                twilight: {
                    900: '#0f0c29', // Deep Indigo/Black
                    800: '#302b63', // Deep Purple
                    700: '#24243e', // Dark Slate
                },
                pastel: {
                    pink: '#ff9a9e',
                    peach: '#fecfef',
                    mint: '#a1c4fd',
                    lavender: '#c2e9fb',
                },
                glass: {
                    100: 'rgba(255, 255, 255, 0.05)',
                    200: 'rgba(255, 255, 255, 0.1)',
                    300: 'rgba(255, 255, 255, 0.15)',
                    dark: 'rgba(15, 12, 41, 0.7)',
                }
            },
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
                serif: ['Playfair Display', 'serif'],
            },
            animation: {
                'blob': 'blob 10s infinite',
                'float': 'float 6s ease-in-out infinite',
                'pulse-slow': 'pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                'glow': 'glow 2s ease-in-out infinite alternate',
            },
            keyframes: {
                blob: {
                    '0%': { transform: 'translate(0px, 0px) scale(1)' },
                    '33%': { transform: 'translate(30px, -50px) scale(1.1)' },
                    '66%': { transform: 'translate(-20px, 20px) scale(0.9)' },
                    '100%': { transform: 'translate(0px, 0px) scale(1)' },
                },
                float: {
                    '0%, 100%': { transform: 'translateY(0)' },
                    '50%': { transform: 'translateY(-20px)' },
                },
                glow: {
                    'from': { boxShadow: '0 0 10px #fff, 0 0 20px #fff, 0 0 30px #e60073' },
                    'to': { boxShadow: '0 0 20px #fff, 0 0 30px #ff4da6, 0 0 40px #ff4da6' },
                }
            }
        },
    },
    plugins: [],
}
