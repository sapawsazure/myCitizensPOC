// Discovers every feature module (src/features/<key>/index.js) at build time.
// Adding a feature = adding a folder; this file is never edited again.
const modules = import.meta.glob('../features/*/index.js', { eager: true, import: 'default' });

export const features = Object.values(modules).sort((a, b) => a.order - b.order);
