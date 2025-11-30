import React, { useEffect, useRef } from 'react';

const NeuralNetwork = ({ isActive = false }) => {
    const canvasRef = useRef(null);

    useEffect(() => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');
        let animationFrameId;
        let nodes = [];
        // Increase node count for "learning" mode
        const nodeCount = isActive ? 60 : 40;
        const connectionDistance = 180;

        // Resize
        const resize = () => {
            canvas.width = canvas.parentElement.clientWidth;
            canvas.height = canvas.parentElement.clientHeight;
        };
        window.addEventListener('resize', resize);
        resize();

        // Node Class
        class Node {
            constructor() {
                this.x = Math.random() * canvas.width;
                this.y = Math.random() * canvas.height;
                // Faster speed if active
                const speedMultiplier = isActive ? 2.5 : 0.5;
                this.vx = (Math.random() - 0.5) * speedMultiplier;
                this.vy = (Math.random() - 0.5) * speedMultiplier;
                this.size = Math.random() * 3 + 2; // Bigger nodes
                this.pulse = 0;
            }

            update() {
                this.x += this.vx;
                this.y += this.vy;

                // Bounce off walls
                if (this.x < 0 || this.x > canvas.width) this.vx *= -1;
                if (this.y < 0 || this.y > canvas.height) this.vy *= -1;

                // Pulse effect
                this.pulse += 0.05;
            }

            draw() {
                ctx.beginPath();
                // Pulsing size
                const currentSize = this.size + Math.sin(this.pulse) * 1;
                ctx.arc(this.x, this.y, Math.max(0, currentSize), 0, Math.PI * 2);

                // Color: Mint/Blue if active, White/Grey if idle
                ctx.fillStyle = isActive ? '#a1c4fd' : 'rgba(255, 255, 255, 0.8)';
                ctx.fill();

                // Glow
                ctx.shadowBlur = isActive ? 15 : 5;
                ctx.shadowColor = isActive ? '#a1c4fd' : 'rgba(255, 255, 255, 0.3)';
            }
        }

        // Init Nodes
        for (let i = 0; i < nodeCount; i++) {
            nodes.push(new Node());
        }

        // Animation Loop
        const animate = () => {
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            // Draw Connections
            for (let i = 0; i < nodes.length; i++) {
                for (let j = i + 1; j < nodes.length; j++) {
                    const dx = nodes[i].x - nodes[j].x;
                    const dy = nodes[i].y - nodes[j].y;
                    const distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < connectionDistance) {
                        ctx.beginPath();
                        ctx.moveTo(nodes[i].x, nodes[i].y);
                        ctx.lineTo(nodes[j].x, nodes[j].y);

                        const opacity = 1 - distance / connectionDistance;

                        // Thicker lines
                        ctx.lineWidth = isActive ? 2 : 1;

                        // Dynamic color for connections
                        if (isActive) {
                            // Glowing "data" effect
                            ctx.strokeStyle = `rgba(161, 196, 253, ${opacity})`;
                        } else {
                            ctx.strokeStyle = `rgba(255, 255, 255, ${opacity * 0.3})`;
                        }

                        ctx.stroke();

                        // "Data Packet" traveling along the line
                        if (isActive && Math.random() > 0.95) {
                            const packetPos = (Date.now() % 1000) / 1000; // Simple loop
                            const px = nodes[i].x + (nodes[j].x - nodes[i].x) * packetPos;
                            const py = nodes[i].y + (nodes[j].y - nodes[i].y) * packetPos;

                            ctx.beginPath();
                            ctx.arc(px, py, 3, 0, Math.PI * 2);
                            ctx.fillStyle = '#fff';
                            ctx.fill();
                        }
                    }
                }
            }

            // Draw Nodes
            nodes.forEach(node => {
                node.update();
                node.draw();
            });

            animationFrameId = requestAnimationFrame(animate);
        };

        animate();

        return () => {
            window.removeEventListener('resize', resize);
            cancelAnimationFrame(animationFrameId);
        };
    }, [isActive]);

    return <canvas ref={canvasRef} className="w-full h-full absolute inset-0 z-0 pointer-events-none" />;
};

export default NeuralNetwork;
