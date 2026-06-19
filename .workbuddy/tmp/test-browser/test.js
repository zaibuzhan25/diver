const { chromium } = require('playwright');
const path = require('path');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  // Capture ALL console messages
  const errors = [];
  const logs = [];
  page.on('console', msg => {
    const type = msg.type();
    const text = msg.text();
    if (type === 'error') errors.push(text);
    else if (text.includes('[诊断]') || text.includes('[全局错误]')) logs.push(text);
  });
  
  page.on('pageerror', err => {
    errors.push(`PAGE ERROR: ${err.message}`);
  });

  // Load the page
  const filePath = 'file:///E:/idea存放/code/diver/src/frontend/user.html';
  console.log('Loading:', filePath);
  
  try {
    await page.goto(filePath, { waitUntil: 'networkidle', timeout: 10000 });
  } catch(e) {
    console.log('Page load timed out or had errors:', e.message);
  }
  
  // Wait a moment for scripts to execute
  await page.waitForTimeout(2000);
  
  // Check diag-status
  const diagStatus = await page.$eval('#diag-status', el => el.textContent).catch(() => 'NOT FOUND');
  console.log('\n=== DIAG STATUS ===');
  console.log(diagStatus);
  
  // Check if viewDestination is defined
  const viewDest = await page.evaluate(() => typeof viewDestination).catch(() => 'ERROR');
  console.log('viewDestination type:', viewDest);
  
  // Check if destinations array has data
  const destCount = await page.evaluate(() => destinations ? destinations.length : -1).catch(() => -1);
  console.log('destinations count:', destCount);
  
  // Check dest-grid content
  const gridHTML = await page.$eval('#dest-grid', el => el.innerHTML.substring(0, 200)).catch(() => 'NOT FOUND');
  console.log('dest-grid first 200 chars:', gridHTML);
  
  // Try to find a dest-card and check its onclick
  const cardOnclick = await page.$eval('.dest-card', el => el.getAttribute('onclick')).catch(() => 'NO CARD');
  console.log('First dest-card onclick:', cardOnclick);
  
  // Try to click a dest-card
  if (cardOnclick !== 'NO CARD') {
    console.log('\n=== CLICK TEST ===');
    try {
      await page.click('.dest-card');
      await page.waitForTimeout(1000);
      
      // Check if detail-overlay became visible
      const overlayVisible = await page.$eval('#detail-overlay', el => {
        const style = window.getComputedStyle(el);
        return style.display !== 'none';
      }).catch(() => false);
      console.log('detail-overlay visible after click:', overlayVisible);
      
      // Check detail-card content
      const cardContent = await page.$eval('#detail-card', el => el.innerHTML.substring(0, 200)).catch(() => 'NOT FOUND');
      console.log('detail-card content:', cardContent);
    } catch(e) {
      console.log('Click failed:', e.message);
    }
  }
  
  // Error summary
  console.log('\n=== ERRORS (' + errors.length + ' total) ===');
  errors.forEach((e, i) => {
    if (i < 10) console.log(`  [${i+1}] ${e.substring(0, 150)}`);
  });
  if (errors.length > 10) console.log(`  ... and ${errors.length - 10} more`);
  
  console.log('\n=== DIAG LOGS ===');
  logs.forEach(l => console.log('  ' + l));
  
  await browser.close();
  console.log('\nDone.');
})().catch(console.error);
