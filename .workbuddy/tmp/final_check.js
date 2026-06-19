const fs = require('fs');
const html = fs.readFileSync('E:/idea存放/code/diver/src/frontend/user.html', 'utf8');

// Check ALL script blocks for potential runtime-breaking issues
const scripts = html.match(/<script[^>]*>([\s\S]*?)<\/script>/gi) || [];

scripts.forEach((s, idx) => {
  const inner = s.replace(/<script[^>]*>/i, '').replace(/<\/script>/i, '');
  
  // Find top-level executable statements that access DOM elements
  const lines = inner.split('\n');
  
  console.log(`\n=== Script Block ${idx + 1} ===`);
  console.log(`Length: ${lines.length} lines`);
  
  // Find lines that call .addEventListener on a getElementById result (pattern: risk)
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (line.match(/getElementById\([^)]+\)\s*\.\s*addEventListener/) && !line.match(/setTimeout|if\s*\(/)) {
      // This is a risky call - element might not exist
      const idMatch = line.match(/getElementById\(['"]([^'"]+)['"]\)/);
      const elemId = idMatch ? idMatch[1] : 'unknown';
      // Check if element is AFTER the script
      const scriptEnd = html.indexOf('</script>', html.indexOf(s));
      const elemAfter = html.indexOf(`id="${elemId}"`, scriptEnd);
      if (elemAfter > scriptEnd) {
        console.log(`  ⚠ LINE ${i+1}: getElementById("${elemId}").addEventListener — ELEMENT IS AFTER SCRIPT (will be null)`);
        console.log(`    ${line.substring(0, 100)}`);
      } else {
        console.log(`  ✓ LINE ${i+1}: getElementById("${elemId}").addEventListener`);
      }
    }
  }
  
  // Find lines that access DOM elements without null checks
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (line.match(/getElementById\([^)]+\)\s*\.\s*(innerHTML|style|classList|value|textContent)\s*=/)) {
      const idMatch = line.match(/getElementById\(['"]([^'"]+)['"]\)/);
      const elemId = idMatch ? idMatch[1] : 'unknown';
      console.log(`  ℹ LINE ${i+1}: getElementById("${elemId}").innerHTML (or similar) = (likely inside function)`);
    }
  }
});

// Final check: does the detail-overlay have the correct CSS to become visible?
console.log('\n=== Detail Overlay Check ===');
const detailOverlayStart = html.indexOf('id="detail-overlay"');
if (detailOverlayStart > 0) {
  const chunk = html.substring(detailOverlayStart, detailOverlayStart + 300);
  console.log('Detail overlay HTML:', chunk.replace(/\n/g, ' '));
  
  // Check if CSS has .detail-overlay.show { display: ... }
  const cssMatch = html.match(/\.detail-overlay\.show\s*\{[^}]+\}/g);
  if (cssMatch) {
    console.log('CSS .detail-overlay.show:', cssMatch);
  } else {
    console.log('WARNING: No .detail-overlay.show CSS rule found!');
  }
}

// Check: does viewDestination add 'show' class?
const viewDestFn = html.match(/async function viewDestination[^}]*detail-overlay[^}]*\}/s);
if (viewDestFn) {
  console.log('viewDestination accesses detail-overlay: YES');
}

console.log('\nCheck complete.');
