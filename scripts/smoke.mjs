// Smoke check for a running Productivity Hub.
// Usage: node scripts/smoke.mjs --expect 3 [--port 8180] [--host localhost]
// Needs no per-phase edits: it reads the feature list from the running backend.

const args = process.argv.slice(2);
const option = (name, fallback) => {
  const index = args.indexOf(`--${name}`);
  return index >= 0 && args[index + 1] ? args[index + 1] : fallback;
};

const host = option('host', 'localhost');
const port = option('port', process.env.SERVER_PORT || '8180');
const expectRaw = option('expect', undefined);
const base = `http://${host}:${port}`;

if (expectRaw === undefined || !Number.isInteger(Number(expectRaw))) {
  console.error('Usage: node scripts/smoke.mjs --expect <number of features> [--port 8180] [--host localhost]');
  process.exit(2);
}
const expected = Number(expectRaw);

const results = [];
const check = (name, ok, detail = '') => results.push({ name, ok, detail });

async function get(path) {
  const response = await fetch(base + path, { headers: { Accept: 'application/json, text/html' } });
  const text = await response.text();
  return { response, text };
}

try {
  const health = await get('/actuator/health');
  const status = JSON.parse(health.text).status;
  check('GET /actuator/health', health.response.ok && status === 'UP', `status ${status}`);

  const featuresCall = await get('/api/features');
  const body = JSON.parse(featuresCall.text);
  const features = body.features ?? [];
  const keys = features.map((feature) => feature.key);
  check('GET /api/features answers 200', featuresCall.response.ok);
  check(`feature count is ${expected}`, body.count === expected && features.length === expected, `count=${body.count}, listed=${features.length}`);
  check('feature keys are unique', new Set(keys).size === keys.length, keys.join(', '));

  for (const feature of features) {
    const call = await get(feature.apiPath);
    let validJson = true;
    try {
      JSON.parse(call.text);
    } catch {
      validJson = false;
    }
    check(`GET ${feature.apiPath} (${feature.name}, phase ${feature.phase})`, call.response.status === 200 && validJson, `HTTP ${call.response.status}`);
  }

  const shell = await get('/');
  check('GET / serves the React shell', shell.response.ok && shell.text.includes('id="root"'), `HTTP ${shell.response.status}`);

  console.log(`App version: ${body.appVersion}`);
} catch (error) {
  check(`reach ${base}`, false, error.message);
}

const width = Math.max(...results.map((r) => r.name.length));
for (const r of results) {
  console.log(`${r.ok ? 'OK  ' : 'FAIL'}  ${r.name.padEnd(width)}  ${r.detail}`);
}
const failed = results.filter((r) => !r.ok).length;
console.log(failed === 0 ? `\nSmoke test passed (${results.length} checks).` : `\nSmoke test FAILED (${failed} of ${results.length} checks).`);
process.exit(failed === 0 ? 0 : 1);
